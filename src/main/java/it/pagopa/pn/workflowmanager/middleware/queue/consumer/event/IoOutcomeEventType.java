package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

public enum IoOutcomeEventType {
    DELIVERED_TO_USER,
    SENDER_NOT_ALLOWED,
    SENT_TO_IO,
    READ,
    PAID
}
