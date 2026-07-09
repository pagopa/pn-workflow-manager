package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnDeliveryNotificationViewedEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NotificationViewedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

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

    @Test
    void handleMessage_withNullPayload_buildsEmptyNotificationViewedInt() {
        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(null));

        // then
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                n.getIun() == null
                && n.getRecipientIndex() == null
                && n.getViewedDate() == null
                && n.getSourceChannel() == null
                && n.getSourceChannelDetails() == null
        ));
    }

    @Test
    void handleMessage_withNullViewedDate_viewedDateIsNull() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0003-202401-X-1")
                .recipientIndex(1)
                .viewedDate(null)
                .sourceChannel("APP_IO")
                .build();

        // when
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));

        // then
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                "ABCD-EFGH-0003-202401-X-1".equals(n.getIun()) && n.getViewedDate() == null
        ));
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
    void handleMessage_withNullIun_skipsAddMdcAndHandlerIsCalledSuccessfully() {
        // given - payload con iun null: la condizione (iun != null && recipientIndex != null) è false,
        // addIunAndRecIndexToMdc non viene invocato e non si genera NPE
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun(null)
                .recipientIndex(0)
                .viewedDate(Instant.parse("2024-01-15T10:00:00Z"))
                .sourceChannel("WEB")
                .build();

        // when / then - nessuna eccezione, l'handler viene comunque chiamato
        consumer.workflowManagerInformalEventConsumer(buildMessage(payload));
        verify(notificationViewedHandler).handleViewNotification(argThat(n ->
                n.getIun() == null && n.getRecipientIndex() == 0
        ));
    }

    @Test
    void handleMessage_handlerThrows_exceptionIsPropagated() {
        // given
        PnDeliveryNotificationViewedEvent.Payload payload = PnDeliveryNotificationViewedEvent.Payload.builder()
                .iun("ABCD-EFGH-0005-202401-X-1")
                .recipientIndex(0)
                .viewedDate(Instant.now())
                .build();
        doThrow(new RuntimeException("handler error"))
                .when(notificationViewedHandler).handleViewNotification(any());

        // when / then
        assertThrows(RuntimeException.class,
                () -> consumer.workflowManagerInformalEventConsumer(buildMessage(payload)));
        verify(notificationViewedHandler).handleViewNotification(any());
    }

    // --- helpers ---

    private Message<PnDeliveryNotificationViewedEvent> buildMessage(PnDeliveryNotificationViewedEvent.Payload payload) {
        PnDeliveryNotificationViewedEvent event = PnDeliveryNotificationViewedEvent.builder()
                .payload(payload)
                .build();
        return MessageBuilder.withPayload(event).build();
    }
}

