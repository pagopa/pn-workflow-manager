package it.pagopa.pn.workflowmanager.action.endworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.*;

@Component
@AllArgsConstructor
@Slf4j
public class EndWorkflowActionHandler {

    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final TimelineUtils timelineUtils;
    private final RecipientDeliveryAnalyzer recipientDeliveryAnalyzer;
    private final TimelineService timelineService;

    public void endWorkflowAction(String iun, int recIndex, String timelineId) {
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
                addTimelineElement(timelineUtils.buildWorkflowEndedReachedTimelineElement(recIndex, notification,
                        getWorkflowEndedReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId),notification);
                break;

            case UNREACHED:
                addTimelineElement(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(recIndex, notification,
                        getWorkflowEndedUnreachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId),notification);
                break;

            case UNDELIVERABLE:
                addTimelineElement(timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(recIndex, notification,
                        getWorkflowEndedUndeliverableTimelineElementId(recIndex, notification.getIun())),notification);
                break;
        }
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        log.info("Creating timeline element with category {} for iun {}", element.getCategory(), element.getIun());
        timelineService.addTimelineElement(element, notification);
    }
}
