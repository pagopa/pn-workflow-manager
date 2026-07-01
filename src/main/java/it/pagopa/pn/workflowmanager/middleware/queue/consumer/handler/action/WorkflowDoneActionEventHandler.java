package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.action.doneworkflow.WorkflowDoneActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class WorkflowDoneActionEventHandler extends AbstractActionEventHandler {
    private final WorkflowDoneActionHandler workflowDoneActionHandler;

    protected WorkflowDoneActionEventHandler(TimelineUtils timelineUtils, WorkflowDoneActionHandler workflowDoneActionHandler) {
        super(timelineUtils);
        this.workflowDoneActionHandler = workflowDoneActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.WORKFLOW_DONE;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.WORKFLOW_DONE.name();
        try {
            log.debug("Handle action of type WORKFLOW_DONE, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            checkWorkflowDoneOrExecute(
                    action,
                    a -> workflowDoneActionHandler.doneWorkflowAction(
                            action.getIun(),
                            action.getRecipientIndex(),
                            action.getTimelineId())
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
