package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.start_workflow.EmailChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.PecChannelSender;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ChannelSenderFactoryTest {

    private IoChannelSender ioChannelSender;
    private PecChannelSender pecChannelSender;
    private EmailChannelSender emailChannelSender;
    private ChannelSenderFactory channelSenderFactory;

    @BeforeEach
    void setUp() {
        ioChannelSender = mock(IoChannelSender.class);
        emailChannelSender = mock(EmailChannelSender.class);
        pecChannelSender = mock(PecChannelSender.class);
        channelSenderFactory = new ChannelSenderFactory(ioChannelSender, emailChannelSender, pecChannelSender);
    }

    @Test
    void shouldReturnIoChannelSender_whenChannelIsIo() {
        // when
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.IO);

        // then
        assertSame(ioChannelSender, result);
    }

    @Test
    void shouldReturnEmailChannelSender_whenChannelIsEmail() {
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.EMAIL);

        assertSame(emailChannelSender, result);
    }

    @Test
    void shouldReturnPecChannelSender_whenChannelIsPec() {
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.PEC);

        assertSame(pecChannelSender, result);
    }
}