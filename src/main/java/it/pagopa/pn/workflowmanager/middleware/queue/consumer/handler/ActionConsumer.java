package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.Action;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.utils.MdcUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class ActionConsumer {

    @SqsListener(value = "${pn.workflow-manager.topics.action-queue}")
    public void workflowManagerActionConsumer(Message<Action> message) {
        setMdc(message);
        final String processName = "WORKFLOW_ACTIONS_INBOUND";
        try {
            log.info("Handle action workflowManagerActionConsumer, with content {}", message);
            log.logStartingProcess(processName);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage() , ex);
            throw ex;
        }
    }
}
