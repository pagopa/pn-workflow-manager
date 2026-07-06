package it.pagopa.pn.workflowmanager.dto.timeline.details;

import lombok.Getter;

@Getter
public enum DigitalChannelsInt {
    APPIO("APPIO"), // TODO: valutare se può essere rinominato in IO
    PEC("PEC"),
    EMAIL("EMAIL"),
    SMS("SMS");

    private final String value;

    DigitalChannelsInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
