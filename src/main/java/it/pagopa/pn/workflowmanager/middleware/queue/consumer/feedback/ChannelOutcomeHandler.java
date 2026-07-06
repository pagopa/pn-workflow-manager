package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger.ChannelEventTriggerDispatcher;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelOutcomeHandler {
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;
    private final SchedulerService schedulerService;
    private final ChannelEventTriggerDispatcher channelEventTriggerDispatcher;
    private final WorkflowUtils workflowUtils;

    public void handleOutcome(NormalizedChannelOutcome normalizedChannelOutcome, NotificationInt notificationInt, Campaign campaign) {
        String iun = normalizedChannelOutcome.getIun();
        int recIndex = normalizedChannelOutcome.getRecIndex();
        ChannelType channel = normalizedChannelOutcome.getChannel();
        log.debug("Handling channel outcome for iun={} channel={} recIndex={}", iun, channel, recIndex);

        timelineService.addTimelineElement(normalizedChannelOutcome.getTimelineElementInternal(), notificationInt);
        NotificationRecipientInt recipient = notificationInt.getRecipients().get(recIndex);
        RecipientTypeInt recipientTypeInt = recipient.getRecipientType();

        if(!CollectionUtils.isEmpty(normalizedChannelOutcome.getTriggers())) {
            log.debug("Dispatching triggers for iun={} channel={} recIndex={}", iun, channel, recIndex);
            channelEventTriggerDispatcher.dispatchAll(normalizedChannelOutcome.getTriggers(), notificationInt);
        }

        FeedbackClassification classification = normalizedChannelOutcome.getClassification();
        if(classification.isRecipientReached()) {
            persistReachedElement(normalizedChannelOutcome, notificationInt);
        }

        boolean isDesiredFeedbackForCampaign = classification.getSatisfiedDesiredFeedback()
                .map(f -> workflowUtils.isDesiredFeedback(campaign, normalizedChannelOutcome.getChannel(), f))
                .orElse(false);
        if(isDesiredFeedbackForCampaign) {
            scheduleWorkflowDone(normalizedChannelOutcome);
        } else if(classification.isFinalFeedback()) {
            workflowUtils.advanceWorkflow(iun, recIndex, channel, campaign, recipientTypeInt);
        }
        // Se non è un feedback finale o un feedback desiderato, devo solo persistere l'elemento in timeline, senza avanzare il workflow.
    }

    private void scheduleWorkflowDone(NormalizedChannelOutcome normalizedChannelEvent) {
        log.info("Scheduling workflow done for iun={} channel={}", normalizedChannelEvent.getIun(), normalizedChannelEvent.getChannel());
        schedulerService.scheduleEvent(
                normalizedChannelEvent.getIun(),
                normalizedChannelEvent.getRecIndex(),
                Instant.now(),
                ActionType.WORKFLOW_DONE,
                normalizedChannelEvent.getTimelineElementInternal().getElementId(),
                new NotHandledDetails()
        );
    }

    private void persistReachedElement(NormalizedChannelOutcome normalizedChannelOutcome, NotificationInt notificationInt) {
        log.debug("Persisting reached timeline element for iun={} channel={} recIndex={}", normalizedChannelOutcome.getIun(), normalizedChannelOutcome.getChannel(), normalizedChannelOutcome.getRecIndex());
        TimelineElementInternal reachedElement = timelineUtils.buildDeliveredTimelineElement(
                notificationInt,
                normalizedChannelOutcome.getRecIndex(),
                normalizedChannelOutcome.getChannel(),
                normalizedChannelOutcome.getTimelineElementInternal().getElementId()
        );
        timelineService.addTimelineElement(reachedElement, notificationInt);

        timelineUtils.handleTransitionToReachedStatusIfNecessary(
                notificationInt,
                normalizedChannelOutcome.getRecIndex(),
                normalizedChannelOutcome.getTimelineElementInternal().getElementId()
        );
    }
}
