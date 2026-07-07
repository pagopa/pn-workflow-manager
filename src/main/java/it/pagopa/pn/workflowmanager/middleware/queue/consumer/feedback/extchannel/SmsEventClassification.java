package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.FeedbackClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum SmsEventClassification implements FeedbackClassification {
    S003(true, false, DesiredFeedbackType.SENT),
    S008(true, false, null),
    S010(true, false, null);

    private final boolean finalFeedback;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    SmsEventClassification(boolean finalFeedback, boolean recipientReached, DesiredFeedbackType desiredFeedback) {
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

    public static SmsEventClassification fromEventCode(String eventCode) {
        try {
            return SmsEventClassification.valueOf(eventCode);
        } catch (IllegalArgumentException ex) {
            throw new PnUnknownEventCodeException(eventCode, ChannelType.SMS);
        }
    }

    public static boolean isSuccessEvent(String eventCode) {
        return S003.name().equals(eventCode);
    }
}

