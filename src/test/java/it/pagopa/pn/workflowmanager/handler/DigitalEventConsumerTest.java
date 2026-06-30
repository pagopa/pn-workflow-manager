package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.DigitalEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class DigitalEventConsumerTest {

    private DigitalEventConsumer digitalEventConsumer;

    @BeforeEach
    void setUp() {
        digitalEventConsumer = new DigitalEventConsumer();
    }

    @Test
    void testWorkflowManagerDigitalEventConsumer_success() {
        Message<String> message = MessageBuilder.withPayload("{\"eventType\":\"DIGITAL\"}")
                .setHeader("aws_messageId", "msg-digital-001")
                .setHeader("X-Amzn-Trace-Id", "trace-digital-001")
                .build();

        assertDoesNotThrow(() -> digitalEventConsumer.workflowManagerDigitalEventConsumer(message));
    }

    @Test
    void testWorkflowManagerDigitalEventConsumer_noHeaders() {
        Message<String> message = MessageBuilder.withPayload("{}").build();

        assertDoesNotThrow(() -> digitalEventConsumer.workflowManagerDigitalEventConsumer(message));
    }
}

