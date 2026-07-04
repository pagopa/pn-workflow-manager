package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.exceptions.PnChannelTriggerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChannelEventTriggerDispatcherTest {

    private static class TriggerA implements ChannelEventTrigger {}
    private static class TriggerB implements ChannelEventTrigger {}

    @Mock
    private ChannelEventTriggerHandler<TriggerA> handlerA;

    @Mock
    private NotificationInt notification;

    private ChannelEventTriggerDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        when(handlerA.getTriggerType()).thenReturn(TriggerA.class);
        dispatcher = new ChannelEventTriggerDispatcher(List.of(handlerA));
    }

    @Test
    void dispatchCallsMatchingHandler() {
        TriggerA trigger = new TriggerA();
        dispatcher.dispatch(trigger, notification);
        verify(handlerA).handle(trigger, notification);
    }

    @Test
    void dispatchThrowsExceptionWhenNoHandlerRegisteredForTriggerType() {
        TriggerB trigger = new TriggerB();
        assertThrows(PnChannelTriggerNotFoundException.class, () -> dispatcher.dispatch(trigger, notification));
    }

    @Test
    void dispatchDoesNotCallHandlerForUnrelatedTriggerType() {
        TriggerB trigger = new TriggerB();
        assertThrows(PnChannelTriggerNotFoundException.class, () -> dispatcher.dispatch(trigger, notification));
        verify(handlerA, never()).handle(any(), any());
    }

    @Test
    void dispatchAllCallsHandlerForEachTrigger() {
        ChannelEventTriggerHandler<TriggerB> handlerB = mock(ChannelEventTriggerHandler.class);
        when(handlerB.getTriggerType()).thenReturn(TriggerB.class);
        dispatcher = new ChannelEventTriggerDispatcher(List.of(handlerA, handlerB));

        TriggerA triggerA = new TriggerA();
        TriggerB triggerB = new TriggerB();

        dispatcher.dispatchAll(Set.of(triggerA, triggerB), notification);

        verify(handlerA).handle(triggerA, notification);
        verify(handlerB).handle(triggerB, notification);
    }

    @Test
    void dispatchAllWithEmptySetDoesNotCallAnyHandler() {
        dispatcher.dispatchAll(Set.of(), notification);
        verify(handlerA, never()).handle(any(), any());
    }

    @Test
    void dispatchAllThrowsExceptionWhenAnyTriggerHasNoHandler() {
        TriggerB unhandledTrigger = new TriggerB();
        assertThrows(PnChannelTriggerNotFoundException.class,
                () -> dispatcher.dispatchAll(Set.of(unhandledTrigger), notification));
    }

    @Test
    void constructorWithMultipleHandlersMapsEachByTriggerType() {
        ChannelEventTriggerHandler<TriggerB> handlerB = mock(ChannelEventTriggerHandler.class);
        when(handlerB.getTriggerType()).thenReturn(TriggerB.class);
        dispatcher = new ChannelEventTriggerDispatcher(List.of(handlerA, handlerB));

        TriggerA triggerA = new TriggerA();
        TriggerB triggerB = new TriggerB();

        dispatcher.dispatch(triggerA, notification);
        dispatcher.dispatch(triggerB, notification);

        verify(handlerA).handle(triggerA, notification);
        verify(handlerB).handle(triggerB, notification);
    }

    @Test
    void constructorWithEmptyHandlerListThrowsExceptionOnAnyDispatch() {
        dispatcher = new ChannelEventTriggerDispatcher(List.of());
        assertThrows(PnChannelTriggerNotFoundException.class, () -> dispatcher.dispatch(new TriggerA(), notification));
    }
}