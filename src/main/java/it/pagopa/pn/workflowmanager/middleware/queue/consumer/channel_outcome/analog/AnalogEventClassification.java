package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.StatusCodeEnum;
import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import lombok.Getter;

import java.util.Optional;

/**
 * Enum that classifies analog (paper-channel) events and implements {@link ChannelOutcomeClassification}.
 * Handles the following scenarios based on {@link StatusCodeEnum}:
 * <ul>
 *   <li>{@code PROGRESS} - the event is a simple progress</li>
 *   <li>{@code OK}       - the event is a final feedback and represents a delivery to the recipient</li>
 *   <li>{@code KO}       - the event is a final feedback of error</li>
 * </ul>
 */
@Getter
public enum AnalogEventClassification implements ChannelOutcomeClassification {

    PROGRESS(false, ChannelOutcomeCategory.progress(), DesiredFeedbackType.SENT),
    OK(true, ChannelOutcomeCategory.positiveFeedback(true), DesiredFeedbackType.RECEIVED),
    KO(false, ChannelOutcomeCategory.negativeFeedback(), null);

    private final ChannelOutcomeCategory category;
    private final boolean recipientReached;
    private final DesiredFeedbackType desiredFeedback;

    AnalogEventClassification(boolean recipientReached, ChannelOutcomeCategory category, DesiredFeedbackType desiredFeedback) {
        this.category = category;
        this.recipientReached = recipientReached;
        this.desiredFeedback = desiredFeedback;
    }

    @Override
    public Optional<DesiredFeedbackType> getSatisfiedDesiredFeedback() {
        return Optional.ofNullable(desiredFeedback);
    }

    /**
     * Get classification from the paper-channel event code.
     *
     * @param statusEventCode the paper-channel event code
     * @return the corresponding {@link AnalogEventClassification}
     * @throws PnUnknownEventCodeException if the event code is not recognized
     */
    public static AnalogEventClassification fromStatusEventCode(String statusEventCode) {
        try {
            return AnalogEventClassification.valueOf(statusEventCode);
        } catch (IllegalArgumentException ex) {
            throw new PnUnknownEventCodeException(statusEventCode, ChannelType.ANALOG);
        }
    }
}