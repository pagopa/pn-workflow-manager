package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.sendcourtesy.SendCourtesyMessageHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.SendCourtesyMessageActionDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class SendCourtesyMessageActionEventHandler extends AbstractActionEventHandler {
    private final SendCourtesyMessageHandler sendCourtesyMessageHandler;

    public SendCourtesyMessageActionEventHandler(TimelineUtils timelineUtils, SendCourtesyMessageHandler sendCourtesyMessageHandler) {
        super(timelineUtils);
        this.sendCourtesyMessageHandler = sendCourtesyMessageHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.SEND_COURTESY_MESSAGE_ACTION;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.SEND_COURTESY_MESSAGE_ACTION.name();

        try {
            log.debug("Handle action of type SEND_COURTESY_MESSAGE_ACTION, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());

            log.logStartingProcess(processName);
            sendCourtesyMessageHandler.handleSendCourtesyMessageAction(action.getIun(), action.getRecipientIndex(), (SendCourtesyMessageActionDetails) action.getDetails());
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
