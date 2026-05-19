package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.IoEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class IoEventConsumerTest {

    private IoEventConsumer ioEventConsumer;

    @BeforeEach
    void setUp() {
        ioEventConsumer = new IoEventConsumer();
    }

    @Test
    void testWorkflowManagerIoEventConsumer_success() {
        Message<String> message = MessageBuilder.withPayload("{\"eventType\":\"IO\"}")
                .setHeader("aws_messageId", "msg-io-001")
                .setHeader("X-Amzn-Trace-Id", "trace-io-001")
                .setHeader("iun", "IUN-IO-001")
                .build();

        assertDoesNotThrow(() -> ioEventConsumer.workflowManagerIoEventConsumer(message));
    }

    @Test
    void testWorkflowManagerIoEventConsumer_noHeaders() {
        Message<String> message = MessageBuilder.withPayload("{}").build();

        assertDoesNotThrow(() -> ioEventConsumer.workflowManagerIoEventConsumer(message));
    }
}

