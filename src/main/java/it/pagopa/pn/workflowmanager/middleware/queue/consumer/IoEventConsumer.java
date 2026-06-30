package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;


@Configuration
@CustomLog
@RequiredArgsConstructor
public class IoEventConsumer {
    @SqsListener(value = "${pn.workflow-manager.topics.io-queue}")
    public void workflowManagerIoEventConsumer(Message<String> message) {
        setMdc(message);
        final String processName = "IO_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerIoEventConsumer, with content {}", message);
            log.logStartingProcess(processName);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }

}
