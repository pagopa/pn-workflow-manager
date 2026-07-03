package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.start_workflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ChannelSenderFactoryTest {

    private IoChannelSender ioChannelSender;
    private ChannelSenderFactory channelSenderFactory;

    @BeforeEach
    void setUp() {
        ioChannelSender = mock(IoChannelSender.class);
        channelSenderFactory = new ChannelSenderFactory(ioChannelSender);
    }

    @Test
    void shouldReturnIoChannelSender_whenChannelIsIo() {
        // when
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.IO);

        // then
        assertSame(ioChannelSender, result);
    }

    @Test
    void shouldThrowIllegalArgumentException_whenChannelIsNotSupported() {
        ChannelType unsupportedChannel = ChannelType.PEC;

        // when / then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> channelSenderFactory.getChannelSender(unsupportedChannel)
        );
        assertTrue(exception.getMessage().contains("Unsupported channel type"));
        assertTrue(exception.getMessage().contains(unsupportedChannel.toString()));
    }
}