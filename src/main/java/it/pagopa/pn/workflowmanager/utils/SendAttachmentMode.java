package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.action.utils.AttachmentType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public record SendAttachmentMode(Set<AttachmentType> types) {

    public boolean includes(AttachmentType type) {
        return types.contains(type);
    }

    /** Parsing da stringa: "COVERPAGE,DOCUMENTS,PAYMENTS" */
    public static SendAttachmentMode fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SendAttachmentMode value cannot be blank");
        }
        Set<AttachmentType> types = Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .map(AttachmentType::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AttachmentType.class)));
        return new SendAttachmentMode(types);
    }
}