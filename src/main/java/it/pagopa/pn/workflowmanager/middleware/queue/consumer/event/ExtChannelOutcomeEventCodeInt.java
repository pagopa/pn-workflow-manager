package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.Getter;

/**
 * Enum that merges event codes from LegalMessageSentDetails and CourtesyMessageProgressEvent.
 * Maps each event code to its corresponding channel type.
 */
@Getter
public enum ExtChannelOutcomeEventCodeInt {
    // PEC events (Legal channel) - start with C
    C000("C000", ChannelType.PEC),
    C001("C001", ChannelType.PEC),
    C002("C002", ChannelType.PEC),
    C003("C003", ChannelType.PEC),
    C004("C004", ChannelType.PEC),
    C005("C005", ChannelType.PEC),
    C006("C006", ChannelType.PEC),
    C007("C007", ChannelType.PEC),
    C008("C008", ChannelType.PEC),
    C009("C009", ChannelType.PEC),
    C010("C010", ChannelType.PEC),
    C011("C011", ChannelType.PEC),
    Q003("Q003", ChannelType.PEC),
    Q010("Q010", ChannelType.PEC),
    Q011("Q011", ChannelType.PEC),

    // EMAIL events (Courtesy channel) - start with M
    M003("M003", ChannelType.EMAIL),
    M004("M004", ChannelType.EMAIL),
    M005("M005", ChannelType.EMAIL),
    M006("M006", ChannelType.EMAIL),
    M008("M008", ChannelType.EMAIL),
    M009("M009", ChannelType.EMAIL),
    M010("M010", ChannelType.EMAIL),
    M011("M011", ChannelType.EMAIL),

    // SMS events (Courtesy channel) - start with S
    S003("S003", ChannelType.SMS),
    S008("S008", ChannelType.SMS),
    S010("S010", ChannelType.SMS);

    private final String value;
    private final ChannelType channel;

    ExtChannelOutcomeEventCodeInt(String value, ChannelType channel) {
        this.value = value;
        this.channel = channel;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public ChannelType getChannelType() {
        return channel;
    }

    /**
     * Get ExtChannelOutcomeEventCodeInt from string value
     * @param value the event code
     * @return the corresponding enum value
     */
    public static ExtChannelOutcomeEventCodeInt fromValue(String value) {
        for (ExtChannelOutcomeEventCodeInt code : ExtChannelOutcomeEventCodeInt.values()) {
            if (code.value.equals(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown event code: " + value);
    }
}

