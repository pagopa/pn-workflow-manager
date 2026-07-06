package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface ChannelEventTriggerHandler<T extends ChannelEventTrigger> {
    Class<T> getTriggerType();
    void handle(T payload, NotificationInt notification);
}