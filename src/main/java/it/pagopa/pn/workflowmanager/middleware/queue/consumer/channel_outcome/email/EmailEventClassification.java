package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum EmailEventClassification implements ChannelOutcomeClassification {
    M003(false, ChannelOutcomeCategory.progress(), DesiredFeedbackType.SENT),
    M004(true, ChannelOutcomeCategory.progress(), DesiredFeedbackType.RECEIVED),
    M005(false, ChannelOutcomeCategory.negativeFeedback(), null),
    M006(false, ChannelOutcomeCategory.negativeFeedback(), null),
    M008(false, ChannelOutcomeCategory.negativeFeedback(), null),
    M009(false, ChannelOutcomeCategory.negativeFeedback(), null),
    M010(false, ChannelOutcomeCategory.negativeFeedback(), null),
    M011(false, ChannelOutcomeCategory.negativeFeedback(), null);

    private final ChannelOutcomeCategory category;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    EmailEventClassification(boolean recipientReached, ChannelOutcomeCategory category, DesiredFeedbackType desiredFeedback) {
        this.recipientReached = recipientReached;
        this.category = category;
        this.desiredFeedback = desiredFeedback;
    }

    @Override
    public Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback() {
        return Optional.ofNullable(desiredFeedback);
    }

    public static EmailEventClassification fromEventCode(String eventCode) {
        try {
            return EmailEventClassification.valueOf(eventCode);
        } catch (IllegalArgumentException ex) {
            throw new PnUnknownEventCodeException(eventCode, ChannelType.EMAIL);
        }
    }
}

