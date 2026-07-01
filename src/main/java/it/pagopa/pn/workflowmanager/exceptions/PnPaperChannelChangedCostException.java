package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_PAPERCHANNELSENDCOSTCHANGED;

public class PnPaperChannelChangedCostException extends PnRuntimeException {

    public PnPaperChannelChangedCostException() {
        this(null);
    }

    public PnPaperChannelChangedCostException(Throwable ex) {
        super("Send cost is different from prepare, need to redo prepare", "Send cost is different from prepare, need to redo prepare", HttpStatus.UNPROCESSABLE_ENTITY.value(), ERROR_CODE_WORKFLOWMANAGER_PAPERCHANNELSENDCOSTCHANGED, null, null, ex);
    }

}
