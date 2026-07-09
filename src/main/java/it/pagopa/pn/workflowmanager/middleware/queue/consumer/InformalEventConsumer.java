package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.api.dto.events.PnDeliveryNotificationViewedEvent;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NotificationViewedHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.Instant;

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
            if (notificationViewed.getIun() != null && notificationViewed.getRecipientIndex() != null) {
                addIunAndRecIndexToMdc(notificationViewed.getIun(), notificationViewed.getRecipientIndex());
            }
            notificationViewedHandler.handleViewNotification(notificationViewed);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            throw ex;
        }
    }

    private NotificationViewedInt mapNotificationViewed(PnDeliveryNotificationViewedEvent event) {
        PnDeliveryNotificationViewedEvent.Payload payload = event.getPayload();
        if (payload == null) {
            return NotificationViewedInt.builder()
                    .build();
        }

        Instant viewedDate = payload.getViewedDate() != null ? payload.getViewedDate() : null;

        return NotificationViewedInt.builder()
                .iun(payload.getIun())
                .recipientIndex(payload.getRecipientIndex())
                .viewedDate(viewedDate)
                .sourceChannel(payload.getSourceChannel())
                .sourceChannelDetails(payload.getSourceChannelDetails())
                .build();
    }
}
