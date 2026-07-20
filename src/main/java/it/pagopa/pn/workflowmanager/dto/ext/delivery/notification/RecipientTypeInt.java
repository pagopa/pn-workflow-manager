package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;

import lombok.Getter;

@Getter
public enum RecipientTypeInt {
    PF("PF"),

    PG("PG");

    private final String value;

    RecipientTypeInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
