package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationUtilsTest {

    @Test
    void getRecipientFromIndexReturnsRecipientAtRequestedPosition() {
        NotificationRecipientInt firstRecipient = NotificationRecipientInt.builder().build();
        NotificationRecipientInt secondRecipient = NotificationRecipientInt.builder().build();
        NotificationInt notification = NotificationInt.builder()
                .recipients(List.of(firstRecipient, secondRecipient))
                .build();

        NotificationRecipientInt result = NotificationUtils.getRecipientFromIndex(notification, 1);

        assertSame(secondRecipient, result);
    }
}