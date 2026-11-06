package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.HandleEventUtils;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.service.mapper.NoticePaymentMapper;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@CustomLog
public class NoticePaymentEventHandler {

    private final PnDeliveryClient pnDeliveryClient;
    private final CampaignService campaignService;
    private final NotificationService notificationService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final WorkflowUtils workflowUtils;

    public void handleResponse(NoticePaymentInt event) {
        HandleEventUtils.addIunToMdc(event.getIun());
        NotificationInt notification = notificationService.getInformalNotificationByIun(event.getIun());

        handleInsertNotificationPaidTimelineElement(event, notification);
        handleInsertPaymentTimelineElement(event, notification);
        handleScheduleWorkflowDone(event);
    }

    private void handleScheduleWorkflowDone(NoticePaymentInt event) {
        InformalSentNotificationV1 informalNotification = pnDeliveryClient.getSentInformalNotification(event.getIun());
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(informalNotification.getCampaignId(), informalNotification.getSenderTaxId());
        if (campaign.getStopOnPaid()) {
            workflowUtils.scheduleWorkflowDone(event.getIun(), event.getRecIndex(), event.getCreditorTaxId(), DesiredFeedbackType.PAID);
        }
    }

    private void handleInsertPaymentTimelineElement(NoticePaymentInt event, NotificationInt notification) {
        NotificationPaidInt notificationPaid = NoticePaymentMapper.mapToNotificationPaidInt(event);
        timelineUtils.buildPaymentTimelineElement(notification, notificationPaid, notification.getRecipients().get(event.getRecIndex()));
    }

    private void handleInsertNotificationPaidTimelineElement(NoticePaymentInt event, NotificationInt notification) {
        log.info("New event PAYMENT, start process to insert: {} ", event.getIuv());

        String elementId = buildTimelineEventIdForNoticePaymentPaid(event);
        TimelineElementInternal timelineElementInternal = timelineUtils.buildNoticePaymentPaidTimelineElement(notification, event, elementId);
        timelineService.addTimelineElement(timelineElementInternal, notification);
        log.info("Payment process complete: {}", event.getIuv());
    }

    public String buildTimelineEventIdForNoticePaymentPaid(NoticePaymentInt element) {
        return TimelineEventId.PAYMENT_NOTICE_PAID.buildEventId(
                EventId.builder()
                        .iun(element.getIun())
                        .recIndex(element.getRecIndex())
                        .creditorTaxId(element.getCreditorTaxId())
                        .noticeCode(element.getNoticeCode())
                        .build());
    }

}
