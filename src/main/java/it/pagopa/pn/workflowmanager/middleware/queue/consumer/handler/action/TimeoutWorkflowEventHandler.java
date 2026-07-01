package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.action.timeoutworkflow.TimeoutWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class TimeoutWorkflowEventHandler extends AbstractActionEventHandler {
    private final TimeoutWorkflowActionHandler timeoutWorkflowActionHandler;

    protected TimeoutWorkflowEventHandler(TimelineUtils timelineUtils, TimeoutWorkflowActionHandler timeoutWorkflowActionHandler) {
        super(timelineUtils);
        this.timeoutWorkflowActionHandler = timeoutWorkflowActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.TIMEOUT_WORKFLOW;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.TIMEOUT_WORKFLOW.name();
        try {
            log.debug("Handle action of type TIMEOUT_WORKFLOW, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            checkWorkflowDoneOrExecute(
                    action,
                    a -> timeoutWorkflowActionHandler.timeoutWorkflowAction(
                            action.getIun(),
                            action.getRecipientIndex(),
                            (TimeoutWorkflowDetails) action.getDetails())
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
