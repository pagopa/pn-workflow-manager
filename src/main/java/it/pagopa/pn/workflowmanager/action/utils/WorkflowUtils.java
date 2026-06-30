package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowUtils {
    private final SchedulerService schedulerService;

    public void scheduleTimeoutForCurrentChannel(String iun, int recIndex, int currentStepIdx, Campaign campaign, ChannelType channel) {
        log.info("Start scheduleTimeoutForCurrentChannel for campaignId={} channel={}", campaign.getCampaignId(), channel);
        Optional<Duration> timeout = getTimeoutForCurrentChannel(campaign, channel);
        if (timeout.isPresent()) {
            Instant timeoutInstant = Instant.now().plus(timeout.get());
            log.debug("Scheduling timeout for campaignId={} channel={} at {}", campaign.getCampaignId(), channel, timeoutInstant);
            TimeoutWorkflowDetails actionDetails = TimeoutWorkflowDetails.builder()
                    .channel(channel)
                    .stepIdx(currentStepIdx)
                    .build();
            schedulerService.scheduleEvent(iun, recIndex, timeoutInstant, ActionType.TIMEOUT_WORKFLOW, actionDetails);
        } else {
            log.info("No timeout defined for campaignId={} channel={}", campaign.getCampaignId(), channel);
        }
    }

    private Optional<Duration> getTimeoutForCurrentChannel(Campaign campaign, ChannelType channel) {
        return Optional.of(getWorkflowEntityForCurrentChannel(campaign, channel))
                .map(WorkFlowEntity::getTimeout);
    }

    private WorkFlowEntity getWorkflowEntityForCurrentChannel(Campaign campaign, ChannelType channel) {
        if(campaign.getWorkflow() == null || campaign.getWorkflow().isEmpty()) {
            throw new PnWorkflowException("No workflow defined for campaignId: " + campaign.getCampaignId());
        }

        return campaign.getWorkflow().stream()
                .filter(workflow -> workflow.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new PnWorkflowException("No workflow entity found for channel: " + channel + " in campaignId: " + campaign.getCampaignId()));
    }
}
