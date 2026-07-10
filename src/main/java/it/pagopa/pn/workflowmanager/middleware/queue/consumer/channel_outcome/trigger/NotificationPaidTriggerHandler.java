package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger;

import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPaidTriggerHandler implements ChannelEventTriggerHandler<NotificationPaidInt> {

    private final NotificationPaidHandler notificationPaidHandler;

    @Override
    public Class<NotificationPaidInt> getTriggerType() {
        return NotificationPaidInt.class;
    }

    @Override
    public void handle(NotificationPaidInt payload, NotificationInt notification) {
        notificationPaidHandler.handlePaymentPaid(payload);
    }
}

