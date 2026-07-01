package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;


import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.action.startworkflow.StartWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class StartWorkflowEventHandler extends AbstractActionEventHandler {
    private final StartWorkflowActionHandler startWorkflowActionHandler;

    protected StartWorkflowEventHandler(TimelineUtils timelineUtils, StartWorkflowActionHandler startWorkflowActionHandler) {
        super(timelineUtils);
        this.startWorkflowActionHandler = startWorkflowActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.START_WORKFLOW;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.START_WORKFLOW.name();

        try {
            log.debug("Handle action of type START_WORKFLOW, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            checkWorkflowDoneOrExecute(
                    action,
                    a -> startWorkflowActionHandler.startWorkflowAction(
                            action.getIun(),
                            action.getRecipientIndex(),
                            (StartWorkflowDetails) action.getDetails())
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
