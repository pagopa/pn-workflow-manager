package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.FeedbackClassification;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

/**
 * Enum that classifies PEC events and implements FeedbackClassification.
 * Handles different scenarios based on the event code:
 * - C000 / C001 / C007: Simple progress events
 * - C003: Final feedback event with successful delivery (RECEIVED)
 * - C011 / C008 / C006 / C010 / C009 / C002 / C004: Error feedback events
 */
@Getter
public enum PecEventClassification implements FeedbackClassification {
    C000(false, false, null),
    C001(false, false, null),
    C007(false, false, null),
    C003(true, true, DesiredFeedbackType.RECEIVED),
    C011(true, false, null),
    C008(true, false, null),
    C006(true, false, null),
    C010(true, false, null),
    C009(true, false, null),
    C002(true, false, null),
    C004(true, false, null);

    private final boolean finalFeedback;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    PecEventClassification(boolean finalFeedback, boolean recipientReached, DesiredFeedbackType desiredFeedback) {
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

    /**
     * Get classification from event code
     * @param eventCode the PEC event code
     * @return the corresponding PecEventClassification
     * @throws IllegalArgumentException if the event code is not recognized
     */
    public static PecEventClassification fromEventCode(String eventCode) {
        try {
            return PecEventClassification.valueOf(eventCode);
        } catch (IllegalArgumentException ex) {
            throw new PnUnknownEventCodeException(eventCode, ChannelType.PEC);
        }
    }

    /**
     * Check if the event code represents a progress event
     * @param eventCode the event code
     * @return true if it's a progress event (C000, C001, C007)
     */
    public static boolean isProgressEvent(String eventCode) {
        return eventCode.equals("C000") || eventCode.equals("C001") || eventCode.equals("C007");
    }

    /**
     * Check if the event code represents a successful delivery
     * @param eventCode the event code
     * @return true if it's a successful delivery (C003)
     */
    public static boolean isSuccessfulDelivery(String eventCode) {
        return C003.name().equals(eventCode);
    }
}

