package it.pagopa.pn.workflowmanager.service;


import it.pagopa.pn.workflowmanager.dto.action.common.Action;

public interface ActionService {
    void addOnlyActionIfAbsent(Action action);
}
