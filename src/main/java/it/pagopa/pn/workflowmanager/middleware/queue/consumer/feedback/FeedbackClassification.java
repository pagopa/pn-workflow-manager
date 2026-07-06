package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback;

import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;

import java.util.Optional;

public interface FeedbackClassification {
    boolean isFinalFeedback();
    boolean isRecipientReached();
    Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback();
}
