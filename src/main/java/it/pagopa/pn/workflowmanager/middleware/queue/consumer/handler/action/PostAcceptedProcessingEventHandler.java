package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;


import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.EventHandler;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class PostAcceptedProcessingEventHandler implements EventHandler<Action> {

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.POST_ACCEPTED_PROCESSING_COMPLETED;
    }

    @Override
    public Class<Action> getPayloadType() {
        return Action.class;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.name();

        try {
            log.debug("Handle action of type ANALOG_WORKFLOW, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());

            log.logStartingProcess(processName);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
