package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.utils.MdcUtils.setMdc;


@Configuration
@CustomLog
@RequiredArgsConstructor
public class DigitalEventConsumer {
    @SqsListener(value = "${pn.workflow-manager.topics.digital-queue}")
    public void workflowManagerDigitalEventConsumer(Message<String> message) {
        setMdc(message);
        final String processName = "DIGITAL_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerDigitalEventConsumer, with content {}", message);
            log.logStartingProcess(processName);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

}
