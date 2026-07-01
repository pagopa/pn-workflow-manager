package it.pagopa.pn.workflowmanager.action.doneworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.exceptions.PnEventRouterException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.getWorkflowDoneReachedTimelineElementId;
import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.getWorkflowDoneUnreachedTimelineElementId;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_ROUTER_EVENT_TYPE_NOT_SUPPORTED;

@Component
@AllArgsConstructor
@Slf4j
public class WorkflowDoneActionHandler {
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final TimelineUtils timelineUtils;
    private final RecipientDeliveryAnalyzer recipientDeliveryAnalyzer;
    private final TimelineService timelineService;

    public void doneWorkflowAction(String iun, int recIndex, String timelineId) {
        log.info("End informal notification workflow for recipient - iun {} id {}", iun, recIndex);

        log.debug("Retrieving notification for iun {}", iun);
        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        RecipientTypeInt currentRecipientType = notificationInt.getRecipients().get(recIndex).getRecipientType();

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId());

        List<TimelineElementInternal> timelineElementsList = new ArrayList<>();
        RecipientDeliveryStatus recipientDeliveryStatus = recipientDeliveryAnalyzer.getDeliveryStatus(
                timelineElementsList, campaign, recIndex, currentRecipientType,notificationInt.getIun());

        log.info("Recipient delivery status for iun {} recIndex {}: {}", notificationInt.getIun(), recIndex, recipientDeliveryStatus);
        createAndPersistTimelineElement(recIndex, recipientDeliveryStatus, timelineId, notificationInt);
    }

    private void createAndPersistTimelineElement(int recIndex, RecipientDeliveryStatus status,
                                                 String sourceTimelineId, NotificationInt notification) {
        switch (status) {
            case REACHED:
                addTimelineElement(timelineUtils.buildWorkflowDoneReachedTimelineElement(recIndex, notification,
                        getWorkflowDoneReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId),notification);
                break;
            case UNREACHED:
                addTimelineElement(timelineUtils.buildWorkflowDoneUnreachedTimelineElement(recIndex, notification,
                        getWorkflowDoneUnreachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId),notification);
                break;

            case UNDELIVERABLE:
                throw new PnEventRouterException(
                        String.format("UNDELIVERABLE status is not supported in WORKFLOW_DONE action for iun=%s recIndex=%d",
                                notification.getIun(), recIndex),
                        ERROR_CODE_WORKFLOWMANAGER_ROUTER_EVENT_TYPE_NOT_SUPPORTED
                );
        }
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        log.info("Creating timeline element with category {} for iun {}", element.getCategory(), element.getIun());
        timelineService.addTimelineElement(element, notification);
    }
}
