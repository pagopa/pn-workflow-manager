package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.PaperChannelUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.PaperChannelHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;


@Configuration
@CustomLog
@RequiredArgsConstructor
public class AnalogEventConsumer {
    private final PaperChannelHandler paperChannelHandler;

    @SqsListener(value = "${pn.workflow-manager.topics.analog-queue}")
    public void workflowManagerAnalogEventConsumer(Message<PaperChannelUpdate> message) {
        setMdc(message);
        final String processName = "ANALOG_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerAnalogEventsConsumer, with content {}", message);
            log.logStartingProcess(processName);
            paperChannelHandler.paperChannelResponseReceiver(message.getPayload());
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

}
