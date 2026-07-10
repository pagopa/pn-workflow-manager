package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger;

import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NotificationViewedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationViewedTriggerHandler implements ChannelEventTriggerHandler<NotificationViewedInt> {

    private final NotificationViewedHandler notificationViewedHandler;

    @Override
    public Class<NotificationViewedInt> getTriggerType() {
        return NotificationViewedInt.class;
    }

    @Override
    public void handle(NotificationViewedInt payload, NotificationInt notification) {
        notificationViewedHandler.handleViewNotification(payload);
    }
}
