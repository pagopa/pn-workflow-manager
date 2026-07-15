package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnDeliveryNotificationViewedEvent;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NotificationViewedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InformalEventConsumerTest {

    private NotificationViewedHandler notificationViewedHandler;
    private InformalEventConsumer consumer;

    @BeforeEach
    void setUp() {
        notificationViewedHandler = Mockito.mock(NotificationViewedHandler.class);
        consumer = new InformalEventConsumer(notificationViewedHandler);
    }

    @Test
    void handleMessage_withFullPayload_mapsAllFields() {
        // given
        Instant viewedDate = Instant.parse("2024-01-15T10:00:00Z");
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0001-202401-X-1")
                .recipientIndex(0)
                .viewedDate(viewedDate)
                .sourceChannel("WEB")
                .sourceChannelDetails("RADD")
                .build();
        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));

        // then
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                "ABCD-EFGH-0001-202401-X-1".equals(n.getIun())
                && n.getRecipientIndex() == 0
                && viewedDate.equals(n.getViewedDate())
                && "WEB".equals(n.getSourceChannel())
                && "RADD".equals(n.getSourceChannelDetails())
        ));
    }

    @Test
    void handleMessage_withMultipleRecipients_mapsRecipientIndexCorrectly() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0002-202401-X-1")
                .recipientIndex(2)
                .viewedDate(Instant.now())
                .sourceChannel("APP_IO")
                .build();

        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));

        // then
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                "ABCD-EFGH-0002-202401-X-1".equals(n.getIun()) && n.getRecipientIndex() == 2
        ));
    }

//    @Test
//    void handleMessage_withNullPayload_throwsException() {
//        // when / then
//        PnInternalException exception = assertThrows(PnInternalException.class,
//                () -> consumer.workflowManagerInformalEventConsumer(buildMessage(null)));
//        assertEquals(ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED, exception.getProblem().getErrors().getFirst().getCode());
//        verifyNoInteractions(notificationViewedHandler);
//    }

    @Test
    void handleMessage_withNullViewedDate_throwsException() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0003-202401-X-1")
                .recipientIndex(1)
                .viewedDate(null)
                .sourceChannel("APP_IO")
                .build();

        // when / then
        PnInternalException exception = assertThrows(PnInternalException.class,
                () -> consumer.workflowManagerInformalEventConsumer(buildMessage(payload)));
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED, exception.getProblem().getErrors().getFirst().getCode());
        verifyNoInteractions(notificationViewedHandler);
    }

    @Test
    void handleMessage_withNullSourceChannelDetails_sourceChannelDetailsIsNull() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0004-202401-X-1")
                .recipientIndex(0)
                .viewedDate(Instant.now())
                .sourceChannel("WEB")
                .sourceChannelDetails(null)
                .build();

        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));

        // then
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                "WEB".equals(n.getSourceChannel()) && n.getSourceChannelDetails() == null
        ));
    }

    @Test
    void handleMessage_withNullIun_throwsException() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun(null)
                .recipientIndex(0)
                .viewedDate(Instant.parse("2024-01-15T10:00:00Z"))
                .sourceChannel("WEB")
                .build();

        // when / then
        PnInternalException exception = assertThrows(PnInternalException.class,
                () -> consumer.workflowManagerInformalEventConsumer(buildMessage(payload)));
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED, exception.getProblem().getErrors().getFirst().getCode());
        verifyNoInteractions(notificationViewedHandler);
    }

    @Test
    void handleMessage_withZeroRecipientIndex_isValid() {
        // given - recipientIndex = 0 is valid (the builder validates >= 0)
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0006-202401-X-1")
                .recipientIndex(0)  // recipientIndex = 0 is the minimum valid value
                .viewedDate(Instant.now())
                .sourceChannel("WEB")
                .build();

        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));

        // then - no exception thrown, handler was invoked successfully
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                "ABCD-EFGH-0006-202401-X-1".equals(n.getIun()) && n.getRecipientIndex() == 0
        ));
    }

    @Test
    void handleMessage_handlerThrows_exceptionIsPropagated() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0005-202401-X-1")
                .recipientIndex(0)
                .viewedDate(Instant.now())
                .sourceChannel("WEB")
                .build();
        doThrow(new RuntimeException("handler error"))
                .when(notificationViewedHandler).handleViewNotification(any());

        // when / then
        assertThrows(RuntimeException.class,
                () -> consumer.workflowManagerInformalEventConsumer(buildMessage(payload)));
        verify(notificationViewedHandler).handleViewNotification(any());
    }

    // --- helpers ---

    private Message<PnDeliveryNotificationViewedEvent.Payload> buildMessage(PnDeliveryNotificationViewedEvent.Payload payload) {

        return MessageBuilder.withPayload(payload).build();
    }
}

