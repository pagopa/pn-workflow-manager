package it.pagopa.pn.workflowmanager.action.endworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryInfo;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.WorkflowClosureTimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class EndWorkflowActionHandler {

    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final RecipientDeliveryAnalyzer recipientDeliveryAnalyzer;
    private final WorkflowClosureTimelineService workflowClosureTimelineService;

    public void endWorkflowAction(List<TimelineElementInternal> timelineElements, String iun, int recIndex) {
        log.info("End informal notification workflow for recipient - iun {} id {}", iun, recIndex);

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        RecipientTypeInt currentRecipientType = notificationInt.getRecipients().get(recIndex).getRecipientType();

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId());

        RecipientDeliveryInfo recipientDeliveryInfo = recipientDeliveryAnalyzer.getDeliveryInfo(
                timelineElements,campaign, recIndex, currentRecipientType);

        log.info("Recipient delivery info for iun {} recIndex {}: {}", notificationInt.getIun(), recIndex, recipientDeliveryInfo);
        createAndPersistTimelineElement(recIndex, recipientDeliveryInfo.status(), recipientDeliveryInfo.sourceElementId(), notificationInt,timelineElements);
    }

    private void createAndPersistTimelineElement(int recIndex, RecipientDeliveryStatus status,
                                                 String sourceTimelineId, NotificationInt notification,
                                                 List<TimelineElementInternal> timelineElements) {
        switch (status) {
            case REACHED:
                workflowClosureTimelineService.saveWorkflowEndedReachedTimelineElement(recIndex, sourceTimelineId, notification, timelineElements);
                break;

            case UNREACHED:
                workflowClosureTimelineService.saveWorkflowEndedUnreachedTimelineElement(recIndex, notification, timelineElements);
                break;

            case UNDELIVERABLE:
                workflowClosureTimelineService.saveWorkflowEndedUndeliverableTimelineElement(recIndex, notification, timelineElements);
                break;
        }
    }
}
