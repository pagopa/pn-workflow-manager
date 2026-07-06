package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.exceptions.PnChannelTriggerNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelEventTriggerDispatcher {
    private final Map<Class<?>, ChannelEventTriggerHandler<?>> handlerMap;

    public ChannelEventTriggerDispatcher(List<ChannelEventTriggerHandler<?>> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        ChannelEventTriggerHandler::getTriggerType,
                        h -> h
                ));
    }

    @SuppressWarnings("unchecked")
    public void dispatch(ChannelEventTrigger trigger, NotificationInt notification) {
        ChannelEventTriggerHandler<ChannelEventTrigger> handler =
                (ChannelEventTriggerHandler<ChannelEventTrigger>) handlerMap.get(trigger.getClass());

        if (handler == null) {
            throw new PnChannelTriggerNotFoundException(trigger.getClass().getName());
        }
        handler.handle(trigger, notification);
    }

    public void dispatchAll(Set<ChannelEventTrigger> Triggers, NotificationInt notification) {
        Triggers.forEach(e -> dispatch(e, notification));
    }
}
