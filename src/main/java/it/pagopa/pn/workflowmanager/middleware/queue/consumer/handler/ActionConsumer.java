package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;


import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.Action;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED;
import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.utils.MdcUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class ActionConsumer {

    @SqsListener(value = "${pn.workflow-manager.topics.pn-workflow-manager-action-queue}")
    public void workflowManagerActionConsumer(Message<Action> message) {
        setMdc(message);
        final String processName = "WORKFLOW_ACTIONS_INBOUND";
        try {
            log.info("Handle action workflowManagerActionConsumer, with content {}", message);
            log.logStartingProcess(processName);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

    private String extractActionType(Action action) {
        String actionType = action.getType() != null ? action.getType().name() : null;
        if (actionType == null) {
            log.error("actionType not present, cannot start scheduled action");
            throw new PnInternalException("actionType not present, cannot start scheduled action", ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED);
        }

        return actionType;
    }
}
