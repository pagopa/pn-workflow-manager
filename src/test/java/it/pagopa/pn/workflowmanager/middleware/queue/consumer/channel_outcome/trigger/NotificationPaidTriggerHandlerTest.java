package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger;

import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPaidTriggerHandlerTest {

    @Mock
    private NotificationPaidHandler notificationPaidHandler;

    @InjectMocks
    private NotificationPaidTriggerHandler triggerHandler;

    @Mock
    private NotificationInt notification;

    @Test
    void getTriggerTypeReturnsNotificationPaidIntClass() {
        assertEquals(NotificationPaidInt.class, triggerHandler.getTriggerType());
    }

    @Test
    void handleDelegatesToNotificationPaidHandler() {
        NotificationPaidInt payload = NotificationPaidInt.builder()
                .iun("IUN_TEST")
                .recIndex(0)
                .build();

        triggerHandler.handle(payload, notification);

        verify(notificationPaidHandler).handlePaymentPaid(payload);
    }
}

