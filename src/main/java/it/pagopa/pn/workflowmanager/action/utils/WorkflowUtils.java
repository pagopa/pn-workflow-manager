package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.WorkflowDoneDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowUtils {
    private final SchedulerService schedulerService;

    public Optional<NextChannel> getNextChannel(Campaign campaign, ChannelType channelType, RecipientTypeInt recipientTypeInt) {
        List<WorkFlowEntity> filteredSteps = campaign.getWorkflow().stream()
                .filter(step -> step.getRecipientType().contains(recipientTypeInt))
                .toList();

        for (int i = 0; i < filteredSteps.size(); i++) {
            if (filteredSteps.get(i).getChannel().equals(channelType)) {
                if (i < filteredSteps.size() - 1) {
                    ChannelType nextChannel = filteredSteps.get(i + 1).getChannel();
                    return Optional.of(new NextChannel(nextChannel, i + 1));
                }
                break;
            }
        }
        return Optional.empty();
    }

    public void scheduleTimeoutForCurrentChannel(String iun, int recIndex, Campaign campaign, ChannelType channel) {
        log.info("Start scheduleTimeoutForCurrentChannel for campaignId={} channel={}", campaign.getCampaignId(), channel);
        Optional<Duration> timeout = getTimeoutForCurrentChannel(campaign, channel);
        if (timeout.isPresent()) {
            Instant timeoutInstant = Instant.now().plus(timeout.get());
            log.debug("Scheduling timeout for campaignId={} channel={} at {}", campaign.getCampaignId(), channel, timeoutInstant);
            TimeoutWorkflowDetails actionDetails = TimeoutWorkflowDetails.builder()
                    .channel(channel)
                    .build();
            schedulerService.scheduleEvent(iun, recIndex, timeoutInstant, ActionType.TIMEOUT_WORKFLOW, actionDetails);
        } else {
            log.info("No timeout defined for campaignId={} channel={}", campaign.getCampaignId(), channel);
        }
    }

    private Optional<Duration> getTimeoutForCurrentChannel(Campaign campaign, ChannelType channel) {
        return Optional.of(getWorkflowEntityForCurrentChannel(campaign, channel))
                .stream()
                .map(WorkFlowEntity::getTimeout)
                .filter(Objects::nonNull)
                .findFirst();
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

    public boolean isDesiredFeedback(Campaign campaign, ChannelType channel, DesiredFeedbackType desiredFeedback) {
        return campaign.getWorkflow().stream()
                .filter(workflowEntity -> workflowEntity.getChannel() == channel)
                .anyMatch(workflowEntity -> workflowEntity.getDesiredFeedback() != null && workflowEntity.getDesiredFeedback().contains(desiredFeedback));
    }

    /**
     * This method is responsible for advancing the workflow to the next channel or scheduling the end of the workflow if no next channel is found.
     *
     * @param iun                The unique identifier for the notification.
     * @param recIndex           The index of the recipient in the notification.
     * @param channel            The current channel type.
     * @param campaign           The campaign associated with the notification.
     * @param recipientTypeInt   The type of the recipient.
     */
    public void advanceWorkflow(String iun, int recIndex, ChannelType channel, Campaign campaign, RecipientTypeInt recipientTypeInt) {
        log.info("Scheduling next channel for iun={} recIndex={} channel={}", iun, recIndex, channel);
        Optional<WorkflowUtils.NextChannel> nextChannelInfoOptional = getNextChannel(campaign, channel, recipientTypeInt);
        if(nextChannelInfoOptional.isEmpty()) {
            scheduleEndWorkflow(iun, recIndex, channel);
        } else {
            WorkflowUtils.NextChannel nextChannelInfo = nextChannelInfoOptional.get();
            scheduleNextChannel(iun, recIndex, nextChannelInfo, channel);
        }
    }

    private void scheduleEndWorkflow(String iun, int recIndex, ChannelType channel) {
        log.info("No next channel found for iun {} and recIndex {} given channel {}. Scheduling END_WORKFLOW event.", iun, recIndex, channel);
        schedulerService.scheduleEvent(
                iun,
                recIndex,
                Instant.now(),
                ActionType.END_WORKFLOW,
                new NotHandledDetails()
        );
    }

    private void scheduleNextChannel(String iun, int recIndex, WorkflowUtils.NextChannel nextChannelInfo, ChannelType previousChannel) {
        log.info("Next channel found {} for iun {} and recIndex {} from previous channel {}. Scheduling START_WORKFLOW event.",
                nextChannelInfo.channel(), iun, recIndex, previousChannel);
        schedulerService.scheduleEvent(
                iun,
                recIndex,
                Instant.now(),
                ActionType.START_WORKFLOW,
                StartWorkflowDetails.builder()
                        .stepIdx(nextChannelInfo.stepIndex())
                        .channel(nextChannelInfo.channel())
                        .build()
        );
    }

    public void scheduleWorkflowDone(String iun,  int recIndex, String elementId, DesiredFeedbackType completionFeedback) {
        log.info("Scheduling workflow done for iun={} recIndex={} triggered by element={}", iun, recIndex, elementId);
        schedulerService.scheduleEvent(
                iun,
                recIndex,
                Instant.now(),
                ActionType.WORKFLOW_DONE,
                elementId,
                WorkflowDoneDetails.builder().completionFeedback(completionFeedback).build()
        );
    }

    public record NextChannel(ChannelType channel, int stepIndex) {
    }
}
