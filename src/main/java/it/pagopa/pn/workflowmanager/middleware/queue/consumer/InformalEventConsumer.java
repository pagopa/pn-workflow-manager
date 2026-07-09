package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnDeliveryNotificationViewedEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NotificationViewedHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.addIunAndRecIndexToMdc;
import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class InformalEventConsumer {

    private final NotificationViewedHandler notificationViewedHandler;

    @SqsListener(value = "${pn.workflow-manager.topics.informal-queue}")
    public void workflowManagerInformalEventConsumer(Message<PnDeliveryNotificationViewedEvent> message) {
        setMdc(message);
        final String processName = "INFORMAL_EVENT_INBOUND";
        try {
            log.info("Handle action workflowManagerInformalEventConsumer, with content {}", message);
            log.logStartingProcess(processName);

            NotificationViewedInt notificationViewed = mapNotificationViewed(message.getPayload());
            addIunAndRecIndexToMdc(notificationViewed.getIun(), notificationViewed.getRecipientIndex());
            notificationViewedHandler.handleViewNotification(notificationViewed);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            throw ex;
        }
    }

    private NotificationViewedInt mapNotificationViewed(PnDeliveryNotificationViewedEvent event) {
        if (event == null) {
            throw invalidEvent("Invalid event received: event must not be null");
        }

        PnDeliveryNotificationViewedEvent.Payload payload = event.getPayload();
        validatePayload(payload);

        return NotificationViewedInt.builder()
                .iun(payload.getIun())
                .recipientIndex(payload.getRecipientIndex())
                .viewedDate(payload.getViewedDate())
                .sourceChannel(payload.getSourceChannel())
                .sourceChannelDetails(payload.getSourceChannelDetails())
                .build();
    }

    private void validatePayload(PnDeliveryNotificationViewedEvent.Payload payload) {
        if (payload == null) {
            throw invalidEvent("Invalid event received: payload must not be null");
        }
        if (payload.getIun() == null) {
            throw invalidEvent("Invalid event received: payload.iun must not be null");
        }
        if (payload.getRecipientIndex() < 0) {
            throw invalidEvent("Invalid event received: payload.recipientIndex must be >= 0");
        }
        if (payload.getViewedDate() == null) {
            throw invalidEvent("Invalid event received: payload.viewedDate must not be null");
        }
        if (payload.getSourceChannel() == null) {
            throw invalidEvent("Invalid event received: payload.sourceChannel must not be null");
        }
    }

    private PnInternalException invalidEvent(String detail) {
        return new PnInternalException(detail, ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED);
    }
}
