package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;


import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public abstract class AbstractActionEventHandler implements EventHandler<Action> {

    protected AbstractActionEventHandler() {
    }

    protected void checkWorkflowDoneOrExecute(Action action, Consumer<Action> functionToCall) {
        //TODO: controllo se il workflow è già stato completato
        if(true) {
            functionToCall.accept(action);
        } else {
            log.info("Workflow is already DONE, the action will not be executed - iun={}", action.getIun());
        }
    }

    public Class<Action> getPayloadType() {
        return Action.class;
    }
}
