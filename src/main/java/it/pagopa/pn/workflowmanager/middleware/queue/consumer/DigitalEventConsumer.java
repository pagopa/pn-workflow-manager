package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.DigitalEventHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;


@Configuration
@CustomLog
@RequiredArgsConstructor
public class DigitalEventConsumer {
    private final DigitalEventHandler digitalEventHandler;

    @SqsListener(value = "${pn.workflow-manager.topics.digital-queue}")
    public void workflowManagerDigitalEventConsumer(Message<SingleStatusUpdate> message) {
        setMdc(message);
        final String processName = "DIGITAL_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerDigitalEventConsumer, with content {}", message);
            log.logStartingProcess(processName);
            digitalEventHandler.handle(message.getPayload());

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

}
