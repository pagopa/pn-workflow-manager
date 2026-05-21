package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.PaperChannelUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.AnalogEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AnalogEventConsumerTest {

    private AnalogEventConsumer analogEventConsumer;

    @BeforeEach
    void setUp() {
        analogEventConsumer = new AnalogEventConsumer();
    }

    @Test
    void testWorkflowManagerAnalogEventConsumer_success() {
        PaperChannelUpdate update = mock(PaperChannelUpdate.class);
        Message<PaperChannelUpdate> message = MessageBuilder.withPayload(update)
                .setHeader("aws_messageId", "msg-analog-001")
                .setHeader("X-Amzn-Trace-Id", "trace-analog-001")
                .setHeader("iun", "IUN-ANALOG-001")
                .build();

        assertDoesNotThrow(() -> analogEventConsumer.workflowManagerAnalogEventConsumer(message));
    }

    @Test
    void testWorkflowManagerAnalogEventConsumer_noHeaders() {
        PaperChannelUpdate update = mock(PaperChannelUpdate.class);
        Message<PaperChannelUpdate> message = MessageBuilder.withPayload(update).build();

        assertDoesNotThrow(() -> analogEventConsumer.workflowManagerAnalogEventConsumer(message));
    }
}

