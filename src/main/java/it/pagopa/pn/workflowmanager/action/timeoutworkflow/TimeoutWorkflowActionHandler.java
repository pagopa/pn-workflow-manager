package it.pagopa.pn.workflowmanager.action.timeoutworkflow;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class TimeoutWorkflowActionHandler {
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final WorkflowUtils workflowUtils;
    private final SchedulerService schedulerService;

    public void timeoutWorkflowAction(String iun, int recIndex, TimeoutWorkflowDetails timeoutWorkflowDetails) {
        log.info("Timeout informal notification workflow for recipient - iun {} id {} channel {}",
                iun, recIndex, timeoutWorkflowDetails.getChannel());

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        NotificationRecipientInt currentRecipient = notificationInt.getRecipients().get(recIndex);

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId()
        );

        processTimeoutAndScheduleNext(iun, recIndex, timeoutWorkflowDetails, campaign, currentRecipient);
    }

    private void processTimeoutAndScheduleNext(String iun, int recIndex, TimeoutWorkflowDetails timeoutWorkflowDetails,
                                               Campaign campaign, NotificationRecipientInt currentRecipient) {

        Optional<WorkflowUtils.NextChannel> nextChannelOpt = workflowUtils.getNextChannel(
                campaign,
                timeoutWorkflowDetails.getChannel(),
                currentRecipient.getRecipientType()
        );

        if (nextChannelOpt.isPresent()) {
            WorkflowUtils.NextChannel nextChannel = nextChannelOpt.get();
            log.info("Next channel found {} for iun {} and recIndex {}. Scheduling START_WORKFLOW event.",
                    nextChannel.channel(), iun, recIndex);
            schedulerService.scheduleEvent(iun, recIndex, Instant.now(), ActionType.START_WORKFLOW, StartWorkflowDetails.builder()
                    .stepIdx(nextChannel.stepIndex())
                    .channel(nextChannel.channel())
                    .build());
        } else {
            log.info("No next channel found for iun {} and recIndex {}. Scheduling END_WORKFLOW event.", iun, recIndex);
            schedulerService.scheduleEvent(iun, recIndex, Instant.now(), ActionType.END_WORKFLOW);
        }
    }
}
