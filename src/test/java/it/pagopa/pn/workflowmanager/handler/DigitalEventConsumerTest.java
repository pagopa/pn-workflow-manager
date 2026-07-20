package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.LegalMessageSentDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.DigitalEventConsumer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.DigitalEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DigitalEventConsumerTest {

    private DigitalEventHandler digitalEventHandler;
    private DigitalEventConsumer digitalEventConsumer;

    @BeforeEach
    void setUp() {
        digitalEventHandler = mock(DigitalEventHandler.class);
        digitalEventConsumer = new DigitalEventConsumer(digitalEventHandler);
    }

    @Test
    void testWorkflowManagerDigitalEventConsumer_success() {
        LegalMessageSentDetails legal = new LegalMessageSentDetails();
        legal.setRequestId("REQ-PEC-001");
        legal.setEventCode(LegalMessageSentDetails.EventCodeEnum.C003);
        legal.setEventTimestamp(Instant.parse("2026-07-07T10:00:00Z"));

        SingleStatusUpdate payload = new SingleStatusUpdate();
        payload.setDigitalLegal(legal);

        Message<SingleStatusUpdate> message = MessageBuilder.withPayload(payload)
                .setHeader("aws_messageId", "msg-digital-001")
                .setHeader("X-Amzn-Trace-Id", "trace-digital-001")
                .build();

        assertDoesNotThrow(() -> digitalEventConsumer.workflowManagerDigitalEventConsumer(message));

        verify(digitalEventHandler).handle(payload);
    }

    @Test
    void testWorkflowManagerDigitalEventConsumer_propagatesException() {
        SingleStatusUpdate payload = new SingleStatusUpdate();

        Message<SingleStatusUpdate> message = MessageBuilder.withPayload(payload)
                .setHeader("aws_messageId", "msg-digital-002")
                .build();

        doThrow(new RuntimeException("handler error")).when(digitalEventHandler).handle(payload);

        assertThrows(RuntimeException.class,
                () -> digitalEventConsumer.workflowManagerDigitalEventConsumer(message));

        verify(digitalEventHandler).handle(payload);
    }
}
