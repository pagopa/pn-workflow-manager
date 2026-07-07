package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.FeedbackClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum EmailEventClassification implements FeedbackClassification {
    M003(false, false, DesiredFeedbackType.SENT),
    M004(false, true, null),
    M005(true, false, null),
    M006(true, false, null),
    M009(true, false, null);

    private final boolean finalFeedback;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    EmailEventClassification(boolean finalFeedback, boolean recipientReached, DesiredFeedbackType desiredFeedback) {
        this.finalFeedback = finalFeedback;
        this.recipientReached = recipientReached;
        this.desiredFeedback = desiredFeedback;
    }

    @Override
    public boolean isFinalFeedback() {
        return finalFeedback;
    }

    @Override
    public boolean isRecipientReached() {
        return recipientReached;
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

    public static boolean isProgressEvent(String eventCode) {
        return M003.name().equals(eventCode) || M004.name().equals(eventCode);
    }
}

