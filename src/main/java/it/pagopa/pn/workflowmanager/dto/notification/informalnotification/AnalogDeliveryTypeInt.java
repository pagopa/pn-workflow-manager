package it.pagopa.pn.workflowmanager.dto.notification.informalnotification;

import lombok.Getter;

@Getter
public enum AnalogDeliveryTypeInt {
    RS("RS");

    private final String value;

    AnalogDeliveryTypeInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
