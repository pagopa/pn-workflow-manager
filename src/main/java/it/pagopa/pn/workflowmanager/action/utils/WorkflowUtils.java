package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.WorkflowDoneDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.*;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowUtils {
    private final SchedulerService schedulerService;

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
        return Optional.of(campaign.getWorkflowByChannel(channel))
                .stream()
                .map(WorkFlowEntity::getTimeout)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public boolean isDesiredFeedback(Campaign campaign, ChannelType channel, DesiredFeedbackType desiredFeedback) {
        WorkFlowEntity workFlow = campaign.getWorkflowByChannel(channel);
        return workFlow.getDesiredFeedback() != null && workFlow.getDesiredFeedback().contains(desiredFeedback);
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
        Optional<NextChannel> nextChannelInfoOptional = campaign.getNextChannel(channel, recipientTypeInt);
        if(nextChannelInfoOptional.isEmpty()) {
            scheduleEndWorkflow(iun, recIndex, channel);
        } else {
            NextChannel nextChannelInfo = nextChannelInfoOptional.get();
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

    private void scheduleNextChannel(String iun, int recIndex, NextChannel nextChannelInfo, ChannelType previousChannel) {
        log.info("Next channel found {} for iun {} and recIndex {} from previous channel {}. Scheduling START_WORKFLOW event.",
                nextChannelInfo.channel(), iun, recIndex, previousChannel);
        scheduleStartWorkflow(iun, recIndex, nextChannelInfo.stepIndex(), nextChannelInfo.channel());
    }

    public void scheduleStartWorkflow(String iun, int recIndex, int stepIdx, ChannelType channel) {
        schedulerService.scheduleEvent(
                iun,
                recIndex,
                Instant.now(),
                ActionType.START_WORKFLOW,
                StartWorkflowDetails.builder()
                        .stepIdx(stepIdx)
                        .channel(channel)
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
}
