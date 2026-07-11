package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum SmsEventClassification implements ChannelOutcomeClassification {
    S003(false, ChannelOutcomeCategory.positiveFeedback(false), DesiredFeedbackType.SENT),
    S008(false, ChannelOutcomeCategory.negativeFeedback(), null),
    S010(false, ChannelOutcomeCategory.negativeFeedback(), null);

    private final ChannelOutcomeCategory category;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    SmsEventClassification(boolean recipientReached, ChannelOutcomeCategory category, DesiredFeedbackType desiredFeedback) {
        this.recipientReached = recipientReached;
        this.category = category;
        this.desiredFeedback = desiredFeedback;
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
}

