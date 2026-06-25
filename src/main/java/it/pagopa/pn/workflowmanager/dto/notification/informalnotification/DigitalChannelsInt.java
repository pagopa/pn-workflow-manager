package it.pagopa.pn.workflowmanager.dto.notification.informalnotification;

import lombok.Getter;

@Getter
public enum DigitalChannelsInt {
    APPIO("APPIO"),
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
