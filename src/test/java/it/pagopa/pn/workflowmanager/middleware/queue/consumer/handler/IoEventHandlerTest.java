package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io.IoEventNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IoEventHandlerTest {
    @Mock
    private IoEventNormalizer ioEventNormalizer;
    @Mock
    private ChannelEventProcessor channelEventProcessor;

    @InjectMocks
    private IoEventHandler ioEventHandler;

    @Mock
    private IoOutcomeEvent event;

    @Test
    void shouldOrchestrateIoEventHandlingSuccessfully() {
        // Act
        ioEventHandler.handle(event);
        // Assert
        verify(channelEventProcessor).process(event, ioEventNormalizer);
    }
}