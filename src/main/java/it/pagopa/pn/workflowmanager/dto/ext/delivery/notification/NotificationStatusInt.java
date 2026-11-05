package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum NotificationStatusInt {
    IN_VALIDATION("IN_VALIDATION"),
    ACCEPTED("ACCEPTED"),
    REFUSED("REFUSED"),
    DELIVERING("DELIVERING"),
    DELIVERED("DELIVERED"),
    VIEWED("VIEWED"),
    EFFECTIVE_DATE("EFFECTIVE_DATE"),
    PAID("PAID"),
    UNREACHABLE("UNREACHABLE"),
    CANCELLED("CANCELLED"),
    RETURNED_TO_SENDER("RETURNED_TO_SENDER"),
    PROCESSING("PROCESSING"),
    COMPLETED_REACHED("COMPLETED_REACHED"),
    COMPLETED_UNREACHED("COMPLETED_UNREACHED"),
    UNDELIVERABLE("UNDELIVERABLE");

    private final String value;
}
