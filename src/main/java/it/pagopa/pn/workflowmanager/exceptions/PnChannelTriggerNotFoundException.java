package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnInternalException;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CHANNEL_TRIGGER_EVENT_NOT_FOUND;

public class PnChannelTriggerNotFoundException extends PnInternalException {
    public PnChannelTriggerNotFoundException(String triggerClassName) {
        super(String.format("ChannelEventTrigger not found for class name '%s'", triggerClassName), ERROR_CODE_WORKFLOWMANAGER_CHANNEL_TRIGGER_EVENT_NOT_FOUND);
    }
}