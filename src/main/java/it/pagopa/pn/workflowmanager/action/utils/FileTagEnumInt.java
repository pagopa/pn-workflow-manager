package it.pagopa.pn.workflowmanager.action.utils;

import lombok.Getter;

@Getter
public enum FileTagEnumInt {
    DOCUMENT("DOCUMENT"),

    ATTACHMENT_PAGOPA("ATTACHMENT_PAGOPA"),

    COVERPAGE("COVERPAGE");


    private final String value;

    FileTagEnumInt(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
