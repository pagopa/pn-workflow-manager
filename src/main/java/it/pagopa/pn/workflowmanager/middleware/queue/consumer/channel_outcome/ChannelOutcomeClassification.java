package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;

import java.util.Optional;

public interface ChannelOutcomeClassification {
    ChannelOutcomeCategory getCategory();
    boolean isRecipientReached();
    Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback();

}
