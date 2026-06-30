package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnInternalException;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_GENERIC_WORKFLOW_ERROR;

public class PnWorkflowException extends PnInternalException {

    public PnWorkflowException(String message) {
        super(message, ERROR_CODE_WORKFLOWMANAGER_GENERIC_WORKFLOW_ERROR);
    }
}
