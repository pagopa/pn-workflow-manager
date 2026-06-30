package it.pagopa.pn.workflowmanager.middleware.queue.consumer.router;

// Dovranno essere censiti qui tutti i tipi di eventi supportati dal router.
public enum SupportedEventType {
    POST_ACCEPTED_PROCESSING_COMPLETED,
    START_WORKFLOW,
    TIMEOUT_WORKFLOW,
    END_WORKFLOW,
    WORKFLOW_DONE
}
