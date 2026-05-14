package it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto;


public interface ActionsPool {
    void addOnlyAction(Action action);
    void unscheduleFutureAction( String actionId );
}
