package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPaidHandler {

    private final NotificationService notificationService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final WorkflowUtils workflowUtils;
    private final CampaignService campaignService;

    public void handlePaymentPaid(NotificationPaidInt payment) {
        log.info("Handling payment for iun={} recIndex={} noticeCode={} creditorTaxId={}",
                payment.getIun(), payment.getRecIndex(), payment.getNoticeCode(), payment.getCreditorTaxId());
        NotificationInt notification = notificationService.getInformalNotificationByIun(payment.getIun());
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notification.getCampaignId(),
                notification.getSender().getPaId());
        NotificationRecipientInt recipient = notification.getRecipients().get(payment.getRecIndex());

        TimelineElementInternal timelineElement = timelineUtils.buildPaymentTimelineElement(notification, payment, recipient);

        if (ObjectUtils.isNotEmpty(campaign) && campaign.getStopOnPaid()) {
            workflowUtils.scheduleWorkflowDone(notification.getIun(), payment.getRecIndex(), timelineElement.getElementId(), DesiredFeedbackType.PAID);
        }

        timelineService.addTimelineElement(timelineElement, notification);
    }
}
