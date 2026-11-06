package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NoticePaymentEventHandlerTest {

    private final PnDeliveryClient deliveryClient = mock(PnDeliveryClient.class);
    private final CampaignService campaignService = mock(CampaignService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final TimelineService timelineService = mock(TimelineService.class);
    private final TimelineUtils timelineUtils = mock(TimelineUtils.class);
    private final WorkflowUtils workflowUtils = mock(WorkflowUtils.class);
    private final NoticePaymentEventHandler handler = new NoticePaymentEventHandler(
            deliveryClient, campaignService, notificationService, timelineService, timelineUtils, workflowUtils);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void handleResponseAddsBothPaymentTimelineElementsAndSchedulesDoneWhenCampaignStopsOnPaid() {
        NoticePaymentInt event = event();
        NotificationInt notification = notification();
        TimelineElementInternal paymentNoticeTimelineElement = TimelineElementInternal.builder()
                .iun("IUN-001")
                .elementId("PAYMENT_NOTICE_PAID")
                .build();
        InformalSentNotificationV1 sentNotification = mock(InformalSentNotificationV1.class);
        Campaign campaign = Campaign.builder().stopOnPaid(true).build();
        when(notificationService.getInformalNotificationByIun("IUN-001")).thenReturn(notification);
        when(timelineUtils.buildNoticePaymentPaidTimelineElement(eq(notification), eq(event), anyString()))
                .thenReturn(paymentNoticeTimelineElement);
        when(deliveryClient.getSentInformalNotification("IUN-001")).thenReturn(sentNotification);
        when(sentNotification.getCampaignId()).thenReturn("CAMPAIGN-001");
        when(sentNotification.getSenderTaxId()).thenReturn("SENDER-001");
        when(campaignService.getCampaignByCampaignIdAndSenderId("CAMPAIGN-001", "SENDER-001"))
                .thenReturn(campaign);

        handler.handleResponse(event);

        verify(timelineUtils).buildNoticePaymentPaidTimelineElement(
                notification, event, handler.buildTimelineEventIdForNoticePaymentPaid(event));
        verify(timelineService).addTimelineElement(paymentNoticeTimelineElement, notification);
        verify(timelineUtils).buildPaymentTimelineElement(eq(notification), argThat(payment ->
                payment.getIun().equals("IUN-001")
                        && payment.getRecIndex() == 0
                        && payment.getNoticeCode().equals("NOTICE-001")
                        && payment.getCreditorTaxId().equals("TAX-ID")
                        && payment.getAmount() == 1200
        ), eq(notification.getRecipients().getFirst()));
        verify(workflowUtils).scheduleWorkflowDone("IUN-001", 0, "TAX-ID", DesiredFeedbackType.PAID);
    }

    @Test
    void handleResponseDoesNotScheduleDoneWhenCampaignDoesNotStopOnPaid() {
        NoticePaymentInt event = event();
        NotificationInt notification = notification();
        InformalSentNotificationV1 sentNotification = mock(InformalSentNotificationV1.class);
        when(notificationService.getInformalNotificationByIun("IUN-001")).thenReturn(notification);
        when(deliveryClient.getSentInformalNotification("IUN-001")).thenReturn(sentNotification);
        when(sentNotification.getCampaignId()).thenReturn("CAMPAIGN-001");
        when(sentNotification.getSenderTaxId()).thenReturn("SENDER-001");
        when(campaignService.getCampaignByCampaignIdAndSenderId(anyString(), anyString()))
                .thenReturn(Campaign.builder().stopOnPaid(false).build());

        handler.handleResponse(event);

        verifyNoInteractions(workflowUtils);
    }

    @Test
    void buildTimelineEventIdUsesNoticePaymentIdentityFields() {
        NoticePaymentInt event = event();

        String eventId = handler.buildTimelineEventIdForNoticePaymentPaid(event);

        assertEquals(TimelineEventId.PAYMENT_NOTICE_PAID.buildEventId(
                EventId.builder()
                        .iun("IUN-001")
                        .recIndex(0)
                        .creditorTaxId("TAX-ID")
                        .noticeCode("NOTICE-001")
                        .build()), eventId);
    }

    private NoticePaymentInt event() {
        return NoticePaymentInt.builder()
                .iun("IUN-001")
                .recIndex(0)
                .noticeCode("NOTICE-001")
                .creditorTaxId("TAX-ID")
                .iuv("IUV-001")
                .amount(1200)
                .paymentDateTime(Instant.parse("2026-10-28T12:00:00Z"))
                .build();
    }

    private NotificationInt notification() {
        return NotificationInt.builder()
                .iun("IUN-001")
                .campaignId("CAMPAIGN-001")
                .sender(NotificationSenderInt.builder().paId("SENDER-001").build())
                .recipients(List.of(NotificationRecipientInt.builder().recipientType(RecipientTypeInt.PF).build()))
                .build();
    }
}
