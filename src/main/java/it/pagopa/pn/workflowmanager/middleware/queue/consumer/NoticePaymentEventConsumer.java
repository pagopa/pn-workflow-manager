package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnNoticePaymentPaidEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NoticePaymentEventHandler;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.HandleEventUtils;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.validator.NoticePaymentPaidEventValidator;
import it.pagopa.pn.workflowmanager.service.mapper.NoticePaymentMapper;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;
import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;

@Configuration
@AllArgsConstructor
@CustomLog
public class NoticePaymentEventConsumer {
    static final String PROCESS_NAME = "NOTICE_PAYMENT_PAID_CONSUMER";
    static final String VALIDATION_ERROR = "NOTICE_PAYMENT_PAID_ERROR";

    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final NoticePaymentPaidEventValidator validator;
    private final NoticePaymentEventHandler noticePaymentEventHandler;

    @SqsListener(queueNames = "#{@pnWorkflowManagerConfigs.topics.noticePayment}")
    public void noticePaymentEventConsumer(Message<PnNoticePaymentPaidEvent.Payload> message) {
        setMdc(message);
        try {
            log.info("Handle message from {} with content {}", "pn-informal-payments-event", message);
            List<String> validationErrors = validator.validate(message.getPayload());
            if (!validationErrors.isEmpty()) {
                log.error("{} validationErrors={}", VALIDATION_ERROR, validationErrors);
                log.logEndingProcess(PROCESS_NAME);
                throw new PnInternalException("Validation errors: " + validationErrors, ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED);
            }
            NoticePaymentInt response = NoticePaymentMapper.messageToInternal(message.getPayload());
            HandleEventUtils.addIunToMdc(response.getIun());
            noticePaymentEventHandler.handleResponse(response);

        } catch (Exception ex) {
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
