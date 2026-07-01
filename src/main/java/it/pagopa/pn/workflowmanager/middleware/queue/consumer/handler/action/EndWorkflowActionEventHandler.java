package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.action.endworkflow.EndWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class EndWorkflowActionEventHandler extends AbstractActionEventHandler {
    private final EndWorkflowActionHandler endWorkflowActionHandler;

    protected EndWorkflowActionEventHandler(TimelineUtils timelineUtils, EndWorkflowActionHandler endWorkflowActionHandler) {
        super(timelineUtils);
        this.endWorkflowActionHandler = endWorkflowActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.END_WORKFLOW;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.END_WORKFLOW.name();
        try {
            log.debug("Handle action of type END_WORKFLOW, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            checkWorkflowDoneOrExecute(
                    action,
                    a -> endWorkflowActionHandler.endWorkflowAction(
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