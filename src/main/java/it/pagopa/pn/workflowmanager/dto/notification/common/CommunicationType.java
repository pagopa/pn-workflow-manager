package it.pagopa.pn.workflowmanager.dto.notification.common;

import lombok.Getter;

@Getter
public enum CommunicationType {
    INFORMAL;

    public static CommunicationType fromValue(String value) {
         if (INFORMAL.name().equals(value)) {
            return INFORMAL;
        }
        throw new IllegalArgumentException("Valore non valido: " + value);
    }
}
