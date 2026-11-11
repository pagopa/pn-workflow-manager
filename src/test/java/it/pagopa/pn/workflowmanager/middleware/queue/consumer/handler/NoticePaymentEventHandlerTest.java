package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.AddTimelineElementResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class NoticePaymentEventHandlerTest {

    @Mock
    private CampaignService campaignService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private WorkflowUtils workflowUtils;

    @InjectMocks
    private NoticePaymentEventHandler handler;

    private NoticePaymentInt event;
    private NotificationInt notification;
    private Campaign campaign;
    private NotificationRecipientInt recipient;

    private static final String IUN = "IUN-123456";
    private static final String CAMPAIGN_ID = "CAMPAIGN-123";
    private static final String PA_ID = "PA-123";
    private static final String CREDITOR_TAX_ID = "12345678901";
    private static final String NOTICE_CODE = "302000100000019421";
    private static final int REC_INDEX = 0;

    @BeforeEach
    void setUp() {
        event = NoticePaymentInt.builder()
                .iun(IUN)
                .recIndex(REC_INDEX)
                .creditorTaxId(CREDITOR_TAX_ID)
                .noticeCode(NOTICE_CODE)
                .amount(1000)
                .build();

        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(PA_ID)
                .build();

        recipient = NotificationRecipientInt.builder()
                .build();

        notification = NotificationInt.builder()
                .campaignId(CAMPAIGN_ID)
                .sender(sender)
                .recipients(List.of(recipient))
                .build();

        campaign = Campaign.builder()
                .campaignId(CAMPAIGN_ID)
                .stopOnPaid(false)
                .build();
    }

    @Test
    void handleResponse_shouldPersistTimelineElements_andScheduleWorkflowDoneWhenCampaignStopOnPaidIsTrue() {
        // Given
        when(notificationService.getInformalNotificationByIun(IUN))
                .thenReturn(notification);

        when(campaignService.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, PA_ID))
                .thenReturn(campaign);

        campaign.setStopOnPaid(true);

        String paymentNoticeTimelineId = "payment-notice-paid-element-id";
        TimelineElementInternal paymentNoticePaidElement = TimelineElementInternal.builder()
                .elementId(paymentNoticeTimelineId)
                .category(TimelineElementCategoryInt.PAYMENT_NOTICE_PAID)
                .build();

        String legacyPaymentTimelineId = "legacy-payment-element-id";
        TimelineElementInternal legacyPaymentElement = TimelineElementInternal.builder()
                .elementId(legacyPaymentTimelineId)
                .category(TimelineElementCategoryInt.PAYMENT)
                .build();

        when(timelineUtils.buildNoticePaymentPaidTimelineElement(notification, event))
                .thenReturn(paymentNoticePaidElement);

        AddTimelineElementResponse addPaymentNoticePaidResponse = new AddTimelineElementResponse(paymentNoticeTimelineId, false);

        when(timelineService.addTimelineElement(paymentNoticePaidElement, notification))
                .thenReturn(addPaymentNoticePaidResponse);

        when(timelineUtils.buildPaymentTimelineElement(
                eq(notification),
                any(NotificationPaidInt.class),
                eq(recipient)))
            .thenReturn(legacyPaymentElement);

        AddTimelineElementResponse addLegacyPaymentResponse = new AddTimelineElementResponse(legacyPaymentTimelineId, false);

        when(timelineService.addTimelineElement(legacyPaymentElement, notification))
                .thenReturn(addLegacyPaymentResponse);

        // When
        handler.handleResponse(event);

        // Then
        verify(notificationService, times(1))
                .getInformalNotificationByIun(IUN);

        verify(timelineUtils, times(1))
                .buildNoticePaymentPaidTimelineElement(notification, event);

        verify(timelineUtils, times(1))
                .buildPaymentTimelineElement(
                        eq(notification),
                        any(NotificationPaidInt.class),
                        eq(recipient));

        verify(timelineService).addTimelineElement(
                paymentNoticePaidElement, notification);

        verify(timelineService).addTimelineElement(
                legacyPaymentElement, notification);

        verify(campaignService).getCampaignByCampaignIdAndSenderId(
                CAMPAIGN_ID, PA_ID);

        verify(workflowUtils).scheduleWorkflowDone(
                IUN,
                REC_INDEX,
                paymentNoticeTimelineId,
                DesiredFeedbackType.PAID
        );

        InOrder inOrder = inOrder(
                notificationService,
                timelineUtils,
                timelineService,
                campaignService,
                workflowUtils
        );

        inOrder.verify(notificationService)
                .getInformalNotificationByIun(IUN);

        inOrder.verify(timelineUtils)
                .buildNoticePaymentPaidTimelineElement(notification, event);

        inOrder.verify(timelineService).addTimelineElement(paymentNoticePaidElement, notification);

        inOrder.verify(timelineUtils).buildPaymentTimelineElement(
            eq(notification),
            any(NotificationPaidInt.class),
            eq(recipient)
        );

        inOrder.verify(timelineService).addTimelineElement(legacyPaymentElement, notification);

        inOrder.verify(campaignService).getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, PA_ID);

        inOrder.verify(workflowUtils).scheduleWorkflowDone(IUN, REC_INDEX, paymentNoticeTimelineId, DesiredFeedbackType.PAID);
    }

    @Test
    void handleResponse_shouldNotScheduleWorkflowDone_whenCampaignStopOnPaidIsFalse() {
        // Given
        when(notificationService.getInformalNotificationByIun(IUN))
                .thenReturn(notification);

        when(campaignService.getCampaignByCampaignIdAndSenderId(CAMPAIGN_ID, PA_ID))
                .thenReturn(campaign);

        String paymentNoticeTimelineId = "payment-notice-paid-element-id";
        TimelineElementInternal paymentNoticePaidElement = TimelineElementInternal.builder()
                .elementId(paymentNoticeTimelineId)
                .category(TimelineElementCategoryInt.PAYMENT_NOTICE_PAID)
                .build();

        String legacyPaymentTimelineId = "legacy-payment-element-id";
        TimelineElementInternal legacyPaymentElement = TimelineElementInternal.builder()
                .elementId(legacyPaymentTimelineId)
                .category(TimelineElementCategoryInt.PAYMENT)
                .build();

        when(timelineUtils.buildNoticePaymentPaidTimelineElement(notification, event))
                .thenReturn(paymentNoticePaidElement);

        AddTimelineElementResponse addPaymentNoticePaidResponse = new AddTimelineElementResponse(legacyPaymentTimelineId, false);

        when(timelineService.addTimelineElement(paymentNoticePaidElement, notification))
                .thenReturn(addPaymentNoticePaidResponse);

        when(timelineUtils.buildPaymentTimelineElement(
                eq(notification),
                any(NotificationPaidInt.class),
                eq(recipient)))
                .thenReturn(legacyPaymentElement);

        AddTimelineElementResponse addLegacyPaymentResponse = new AddTimelineElementResponse(paymentNoticeTimelineId, false);

        when(timelineService.addTimelineElement(legacyPaymentElement, notification))
                .thenReturn(addLegacyPaymentResponse);

        // When
        handler.handleResponse(event);

        // Then
        verify(timelineService).addTimelineElement(
                paymentNoticePaidElement, notification);

        verify(timelineService).addTimelineElement(
                legacyPaymentElement, notification);

        verify(campaignService).getCampaignByCampaignIdAndSenderId(
                CAMPAIGN_ID, PA_ID);

        verify(workflowUtils, never()).scheduleWorkflowDone(
                anyString(),
                anyInt(),
                anyString(),
                any(DesiredFeedbackType.class)
        );
    }
}
