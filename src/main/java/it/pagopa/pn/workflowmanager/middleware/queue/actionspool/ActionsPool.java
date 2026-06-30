package it.pagopa.pn.workflowmanager.middleware.queue.actionspool;


import it.pagopa.pn.workflowmanager.dto.action.common.Action;

public interface ActionsPool {
    void addOnlyAction(Action action);
}
