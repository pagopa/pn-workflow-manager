package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class NotificationUtilsTest {

    private final NotificationUtils notificationUtils = new NotificationUtils();

    @Test
    void getRecipientFromIndexReturnsRecipientAtRequestedPosition() {
        NotificationRecipientInt firstRecipient = NotificationRecipientInt.builder().build();
        NotificationRecipientInt secondRecipient = NotificationRecipientInt.builder().build();
        NotificationInt notification = NotificationInt.builder()
                .recipients(List.of(firstRecipient, secondRecipient))
                .build();

        NotificationRecipientInt result = notificationUtils.getRecipientFromIndex(notification, 1);

        assertSame(secondRecipient, result);
    }
}

