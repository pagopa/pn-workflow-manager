package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.sendchannelmessage.SendChannelMessageActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.actionmanager.model.ActionType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CustomLog
public class SendChannelMessageEventHandler extends AbstractActionEventHandler {
    private final SendChannelMessageActionHandler sendChannelMessageActionHandler;

    protected SendChannelMessageEventHandler(TimelineUtils timelineUtils,
                                             SendChannelMessageActionHandler sendChannelMessageActionHandler) {
        super(timelineUtils);
        this.sendChannelMessageActionHandler = sendChannelMessageActionHandler;
    }

    @Override
    public SupportedEventType getSupportedEventType() {
        return SupportedEventType.SEND_CHANNEL_MESSAGE;
    }

    @Override
    public void handle(Action action, MessageHeaders headers) {
        final String processName = ActionType.SEND_CHANNEL_MESSAGE.name();

        try {
            log.debug("Handle action of type SEND_CHANNEL_MESSAGE, with payload {}", action);
            MdcUtils.addIunAndRecIndexAndCorrIdToMdc(action.getIun(), action.getRecipientIndex(), action.getActionId());
            log.logStartingProcess(processName);
            List<TimelineElementInternal> timelineElements = timelineUtils.getTimelineElementInternals(action.getIun()).toList();
            checkWorkflowDoneOrExecute(
                    timelineElements,
                    action,
                    a -> sendChannelMessageActionHandler.sendChannelMessageAction(
                            action.getIun(),
                            action.getRecipientIndex(),
                            (SendChannelMessageDetails) action.getDetails())
            );
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            MdcUtils.handleException(headers, ex);
            throw ex;
        }
    }
}
