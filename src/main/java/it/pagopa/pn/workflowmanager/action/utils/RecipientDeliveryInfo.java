package it.pagopa.pn.workflowmanager.action.utils;

public record RecipientDeliveryInfo(RecipientDeliveryStatus status, String sourceElementId) {
    public RecipientDeliveryInfo(RecipientDeliveryStatus status) {
        this(status, null);
    }
}
