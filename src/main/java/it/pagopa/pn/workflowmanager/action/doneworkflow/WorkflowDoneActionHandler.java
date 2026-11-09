package it.pagopa.pn.workflowmanager.action.doneworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryInfo;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.dto.action.details.WorkflowDoneDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.exceptions.PnEventRouterException;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.WorkflowClosureTimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_ROUTER_EVENT_TYPE_NOT_SUPPORTED;

@Component
@AllArgsConstructor
@Slf4j
public class WorkflowDoneActionHandler {
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final RecipientDeliveryAnalyzer recipientDeliveryAnalyzer;
    private final WorkflowClosureTimelineService workflowClosureTimelineService;

    public void doneWorkflowAction(List<TimelineElementInternal> timelineElements, String iun, int recIndex, String timelineId, WorkflowDoneDetails details) {
        log.info("Done informal notification workflow for recipient - iun {} id {}", iun, recIndex);

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        RecipientTypeInt currentRecipientType = notificationInt.getRecipients().get(recIndex).getRecipientType();

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId());

        RecipientDeliveryInfo recipientDeliveryInfo = recipientDeliveryAnalyzer.getDeliveryInfo(timelineElements,
                campaign, recIndex, currentRecipientType);

        log.info("Recipient delivery info for iun {} recIndex {}: {}", notificationInt.getIun(), recIndex, recipientDeliveryInfo);
        createAndPersistTimelineElement(recIndex, recipientDeliveryInfo.status(), timelineId, notificationInt, details.getCompletionFeedback(), timelineElements);
    }

    private void createAndPersistTimelineElement(
            int recIndex,
            RecipientDeliveryStatus status,
            String sourceTimelineId,
            NotificationInt notification,
            DesiredFeedbackType completionFeedback,
            List<TimelineElementInternal> timelineElements
    ) {
        switch (status) {
            case REACHED:
                workflowClosureTimelineService.saveWorkflowDoneReachedTimelineElement(recIndex, sourceTimelineId, notification, completionFeedback, timelineElements);
                break;
            case UNREACHED:
                workflowClosureTimelineService.saveWorkflowDoneUnreachedTimelineElement(recIndex, sourceTimelineId, notification, completionFeedback, timelineElements);
                break;

            case UNDELIVERABLE:
                throw new PnEventRouterException(
                        String.format("UNDELIVERABLE status is not supported in WORKFLOW_DONE action for iun=%s recIndex=%d",
                                notification.getIun(), recIndex),
                        ERROR_CODE_WORKFLOWMANAGER_ROUTER_EVENT_TYPE_NOT_SUPPORTED
                );
        }
    }
}