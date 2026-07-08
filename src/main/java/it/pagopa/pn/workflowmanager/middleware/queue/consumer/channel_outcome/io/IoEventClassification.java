package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum IoEventClassification implements ChannelOutcomeClassification {
    DELIVERED_TO_USER(true, ChannelOutcomeCategory.PROGRESS, DesiredFeedbackType.RECEIVED),
    SENDER_NOT_ALLOWED(false, ChannelOutcomeCategory.FEEDBACK, null),
    SENT_TO_IO(false, ChannelOutcomeCategory.PROGRESS, null),
    READ(true, ChannelOutcomeCategory.PROGRESS, DesiredFeedbackType.READ),
    PAID(true, ChannelOutcomeCategory.PROGRESS, DesiredFeedbackType.PAID);

    private final boolean recipientReached;
    private final ChannelOutcomeCategory category;
    private final DesiredFeedbackType desiredFeedback;

    public Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback() {
        return Optional.ofNullable(desiredFeedback);
    }

    IoEventClassification(boolean recipientReached, ChannelOutcomeCategory category, DesiredFeedbackType desiredFeedback) {
        this.recipientReached = recipientReached;
        this.category = category;
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
