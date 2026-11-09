package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.AddTimelineElementResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
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
    private final CampaignService campaignService;
    private final NotificationService notificationService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final WorkflowUtils workflowUtils;

    public void handleResponse(NoticePaymentInt event) {
        String iun = event.getIun();
        log.info("New event PAYMENT, start process to insert on notification: {} for recIndex: {}, noticeCode: {} and creditorTaxId: {}", iun, event.getRecIndex(), event.getNoticeCode(), event.getCreditorTaxId());

        NotificationInt notification = notificationService.getInformalNotificationByIun(iun);

        String noticePaymentPaidElementId = insertNoticePaymentPaidTimelineElement(event, notification);
        insertLegacyPaymentTimelineElement(event, notification);
        handleScheduleWorkflowDone(event, notification, noticePaymentPaidElementId);
        log.info("Payment process completed for notification: {} and recIndex: {}", iun, event.getRecIndex());
    }

    private void handleScheduleWorkflowDone(NoticePaymentInt event, NotificationInt notificationInt, String noticePaymentPaidElementId) {
        log.debug("Start handleScheduleWorkflowDone for notification: {} and recIndex: {}", event.getIun(), event.getRecIndex());
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(notificationInt.getCampaignId(), notificationInt.getSender().getPaId());
        if (campaign.getStopOnPaid()) {
            workflowUtils.scheduleWorkflowDone(event.getIun(), event.getRecIndex(), noticePaymentPaidElementId, DesiredFeedbackType.PAID);
        }
    }

    private void insertLegacyPaymentTimelineElement(NoticePaymentInt event, NotificationInt notification) {
        NotificationPaidInt notificationPaid = NoticePaymentMapper.mapToNotificationPaidInt(event);
        timelineService.addTimelineElement(
            timelineUtils.buildPaymentTimelineElement(notification, notificationPaid, notification.getRecipients().get(event.getRecIndex())),
            notification
        );
    }

    private String insertNoticePaymentPaidTimelineElement(NoticePaymentInt event, NotificationInt notification) {
        AddTimelineElementResponse persistedTimelineElement = timelineService.addTimelineElement(
                timelineUtils.buildNoticePaymentPaidTimelineElement(notification, event),
                notification
        );
        return persistedTimelineElement.getTimelineElementId();
    }

}
