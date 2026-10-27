package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPaidHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private WorkflowUtils workflowUtils;

    @Mock
    private CampaignService campaignService;

    @InjectMocks
    private NotificationPaidHandler notificationPaidHandler;

    private NotificationInt notification;
    private NotificationPaidInt payment;
    private TimelineElementInternal builtTimelineElement;

    @BeforeEach
    void setUp() {
        Instant sentAt = Instant.parse("2026-07-10T09:00:00Z");
        Instant eventTimestamp = Instant.parse("2026-07-10T09:30:00Z");

        notification = NotificationInt.builder()
                .iun("IUN_TEST")
                .campaignId("CAMPAIGN_ID")
                .sentAt(sentAt)
                .sender(NotificationSenderInt.builder().paId("PA_ID").build())
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .recipientType(RecipientTypeInt.PF)
                                .build()
                ))
                .build();

        payment = NotificationPaidInt.builder()
                .iun("IUN_TEST")
                .recIndex(0)
                .noticeCode("NOTICE_123")
                .creditorTaxId("CREDITOR_456")
                .paymentSourceChannel("IO")
                .amount(100)
                .eventTimestamp(eventTimestamp)
                .build();

        builtTimelineElement = TimelineElementInternal.builder()
                .iun("IUN_TEST")
                .elementId("NOTIFICATION_PAID.IUN_IUN_TEST.CODE_PPANOTICE_123CREDITOR_456")
                .category(TimelineElementCategoryInt.PAYMENT)
                .build();

        when(notificationService.getInformalNotificationByIun("IUN_TEST")).thenReturn(notification);
        when(timelineUtils.buildPaymentTimelineElement(eq(notification), eq(payment), eq(notification.getRecipients().getFirst())))
                .thenReturn(builtTimelineElement);
    }

    @Test
    void handlePaymentPaidBuildsAndPersistsPaymentTimelineElementWhenCampaignIsAbsent() {
        when(campaignService.getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID"))
                .thenReturn(null);

        notificationPaidHandler.handlePaymentPaid(payment);

        verify(campaignService).getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID");
        verify(timelineUtils).buildPaymentTimelineElement(eq(notification), eq(payment), eq(notification.getRecipients().getFirst()));
        verify(timelineService).addTimelineElement(eq(builtTimelineElement), eq(notification));
        verifyNoInteractions(workflowUtils);
    }

    @Test
    void handlePaymentPaidSchedulesWorkflowDoneWhenStopOnPaidIsEnabled() {
        Campaign campaign = Campaign.builder().stopOnPaid(true).build();
        when(campaignService.getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID"))
                .thenReturn(campaign);

        notificationPaidHandler.handlePaymentPaid(payment);

        verify(campaignService).getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID");
        verify(workflowUtils).scheduleWorkflowDone(
                notification.getIun(), payment.getRecIndex(), builtTimelineElement.getElementId(), DesiredFeedbackType.PAID);
        verify(timelineService).addTimelineElement(builtTimelineElement, notification);
    }

    @Test
    void handlePaymentPaidDoesNotScheduleWorkflowDoneWhenStopOnPaidIsDisabled() {
        Campaign campaign = Campaign.builder().stopOnPaid(false).build();
        when(campaignService.getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID"))
                .thenReturn(campaign);

        notificationPaidHandler.handlePaymentPaid(payment);

        verify(campaignService).getCampaignByCampaignIdAndSenderId("CAMPAIGN_ID", "PA_ID");
        verify(timelineService).addTimelineElement(builtTimelineElement, notification);
        verifyNoInteractions(workflowUtils);
    }
}
