package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.documentcreation.DocumentCreationResponseHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CustomLog
public class DocumentCreationResponseEventHandler extends AbstractActionEventHandler {
    private final DocumentCreationResponseHandler documentCreationResponseHandler;

    protected DocumentCreationResponseEventHandler(TimelineUtils timelineUtils, DocumentCreationResponseHandler documentCreationResponseHandler) {
        super(timelineUtils);
        this.documentCreationResponseHandler = documentCreationResponseHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.DOCUMENT_CREATION_RESPONSE;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.DOCUMENT_CREATION_RESPONSE.name();
        try {
            log.debug("Handle action of type DOCUMENT_CREATION_RESPONSE, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            List<TimelineElementInternal> timelineElements = timelineUtils.getTimelineElementInternals(action.getIun()).toList();
            checkWorkflowDoneOrExecute(
                    timelineElements,
                    action,
                    a -> documentCreationResponseHandler.handleResponseReceived(
                            action.getIun(),
                            action.getRecipientIndex(),
                            (DocumentCreationResponseActionDetails) action.getDetails()
                    )
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
