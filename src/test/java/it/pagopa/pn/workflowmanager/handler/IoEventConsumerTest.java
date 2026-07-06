package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.IoEventConsumer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.IoEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IoEventConsumerTest {
    private IoEventHandler ioEventHandler;

    private IoEventConsumer ioEventConsumer;

    @BeforeEach
    void setUp() {
        ioEventHandler = mock(IoEventHandler.class);
        ioEventConsumer = new IoEventConsumer(ioEventHandler);
    }

    @Test
    void testWorkflowManagerIoEventConsumer_success() {
        IoOutcomeEvent event = IoOutcomeEvent.builder().build();
        Message<IoOutcomeEvent> message = MessageBuilder.withPayload(event)
                .setHeader("aws_messageId", "msg-io-001")
                .setHeader("X-Amzn-Trace-Id", "trace-io-001")
                .setHeader("iun", "IUN-IO-001")
                .build();

        assertDoesNotThrow(() -> ioEventConsumer.workflowManagerIoEventConsumer(message));

        verify(ioEventHandler).handle(event);
    }
}

