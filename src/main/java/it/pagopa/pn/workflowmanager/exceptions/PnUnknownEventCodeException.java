package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UNKNOWN_EVENT_CODE;

public class PnUnknownEventCodeException extends PnInternalException {
    public PnUnknownEventCodeException(String eventCode, ChannelType channel) {
        super(String.format("EventCode '%s' not recognized for channel %s", eventCode, channel), ERROR_CODE_WORKFLOWMANAGER_UNKNOWN_EVENT_CODE);
    }
}