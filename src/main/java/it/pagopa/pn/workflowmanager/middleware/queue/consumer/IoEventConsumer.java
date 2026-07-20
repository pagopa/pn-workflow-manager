package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.IoEventHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;


@Configuration
@CustomLog
@RequiredArgsConstructor
public class IoEventConsumer {
    private final IoEventHandler ioEventHandler;

    @SqsListener(value = "${pn.workflow-manager.topics.io-queue}")
    public void workflowManagerIoEventConsumer(Message<IoOutcomeEvent> message) {
        setMdc(message);
        final String processName = "IO_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerIoEventConsumer, with content {}", message);
            log.logStartingProcess(processName);
            ioEventHandler.handle(message.getPayload());
            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

}
