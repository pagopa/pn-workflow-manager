package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.PaperChannelUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.AnalogEventConsumer;
import it.pagopa.pn.workflowmanager.middleware.responsehandler.PaperChannelResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalogEventConsumerTest {

    @Mock
    private PaperChannelResponseHandler paperChannelResponseHandler;

    private AnalogEventConsumer analogEventConsumer;

    @BeforeEach
    void setUp() {
        analogEventConsumer = new AnalogEventConsumer(paperChannelResponseHandler);
    }

    @Test
    void testWorkflowManagerAnalogEventConsumer_success() {
        PaperChannelUpdate update = mock(PaperChannelUpdate.class);
        Message<PaperChannelUpdate> message = MessageBuilder.withPayload(update)
                .setHeader("aws_messageId", "msg-analog-001")
                .setHeader("X-Amzn-Trace-Id", "trace-analog-001")
                .setHeader("iun", "IUN-ANALOG-001")
                .build();

        doNothing().when(paperChannelResponseHandler).paperChannelResponseReceiver(update);

        assertDoesNotThrow(() -> analogEventConsumer.workflowManagerAnalogEventConsumer(message));

        verify(paperChannelResponseHandler, times(1)).paperChannelResponseReceiver(update);
    }

    @Test
    void testWorkflowManagerAnalogEventConsumer_noHeaders() {
        PaperChannelUpdate update = mock(PaperChannelUpdate.class);
        Message<PaperChannelUpdate> message = MessageBuilder.withPayload(update).build();

        doNothing().when(paperChannelResponseHandler).paperChannelResponseReceiver(update);

        assertDoesNotThrow(() -> analogEventConsumer.workflowManagerAnalogEventConsumer(message));

        verify(paperChannelResponseHandler, times(1)).paperChannelResponseReceiver(update);
    }
}