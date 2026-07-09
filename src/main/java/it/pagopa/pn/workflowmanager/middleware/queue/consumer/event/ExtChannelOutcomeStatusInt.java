package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import lombok.Getter;

@Getter
public enum ExtChannelOutcomeStatusInt {
    PROGRESS("PROGRESS"),

    OK("OK"),

    ERROR("ERROR");

    private final String value;

    ExtChannelOutcomeStatusInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
