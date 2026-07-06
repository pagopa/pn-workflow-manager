package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.start_workflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.action.start_workflow.PecChannelSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ChannelSenderFactoryTest {

    private IoChannelSender ioChannelSender;
    private PecChannelSender pecChannelSender;
    private ChannelSenderFactory channelSenderFactory;

    @BeforeEach
    void setUp() {
        ioChannelSender = mock(IoChannelSender.class);
        pecChannelSender = mock(PecChannelSender.class);
        channelSenderFactory = new ChannelSenderFactory(ioChannelSender, pecChannelSender);
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
        ChannelType unsupportedChannel = ChannelType.EMAIL;

        // when / then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> channelSenderFactory.getChannelSender(unsupportedChannel)
        );
        assertTrue(exception.getMessage().contains("Unsupported channel type"));
        assertTrue(exception.getMessage().contains(unsupportedChannel.toString()));
    }

    @Test
    void shouldReturnPecChannelSender_whenChannelIsPec() {
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.PEC);

        assertSame(pecChannelSender, result);
    }
}