package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.io;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.FeedbackClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum IoEventClassification implements FeedbackClassification {
    DELIVERED_TO_USER(true, false, DesiredFeedbackType.RECEIVED),
    SENDER_NOT_ALLOWED(false, true, null),
    SENT_TO_IO(false, false, null),
    READ(true, false, DesiredFeedbackType.READ),
    PAID(true, false, DesiredFeedbackType.PAID);

    private final boolean recipientReached;
    private final boolean finalFeedback;
    private final DesiredFeedbackType desiredFeedback;

    public Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback() {
        return Optional.ofNullable(desiredFeedback);
    }

    IoEventClassification(boolean recipientReached, boolean finalFeedback, DesiredFeedbackType desiredFeedback) {
        this.recipientReached = recipientReached;
        this.finalFeedback = finalFeedback;
        this.desiredFeedback = desiredFeedback;
    }

    public static IoEventClassification fromEventType(String value) {
        for (IoEventClassification classification : IoEventClassification.values()) {
            if (classification.name().equalsIgnoreCase(value)) {
                return classification;
            }
        }
        throw new PnUnknownEventCodeException(value, ChannelType.IO);
    }
}
