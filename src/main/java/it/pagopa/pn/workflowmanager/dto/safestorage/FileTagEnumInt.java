package it.pagopa.pn.workflowmanager.dto.safestorage;

import lombok.Getter;

@Getter
public enum FileTagEnumInt {
    DOCUMENT("DOCUMENT"),

    ATTACHMENT_PAGOPA("ATTACHMENT_PAGOPA");

    private final String value;

    FileTagEnumInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
