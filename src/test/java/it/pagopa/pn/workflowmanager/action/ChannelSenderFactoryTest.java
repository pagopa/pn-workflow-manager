package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.startworkflow.AnalogChannelSender;
import it.pagopa.pn.workflowmanager.action.startworkflow.EmailChannelSender;
import it.pagopa.pn.workflowmanager.action.startworkflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.action.startworkflow.PecChannelSender;
import it.pagopa.pn.workflowmanager.action.startworkflow.SmsChannelSender;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ChannelSenderFactoryTest {

    private IoChannelSender ioChannelSender;
    private PecChannelSender pecChannelSender;
    private EmailChannelSender emailChannelSender;
    private SmsChannelSender smsChannelSender;
    private ChannelSenderFactory channelSenderFactory;
    private AnalogChannelSender analogChannelSender;

    @BeforeEach
    void setUp() {
        ioChannelSender = mock(IoChannelSender.class);
        emailChannelSender = mock(EmailChannelSender.class);
        pecChannelSender = mock(PecChannelSender.class);
        smsChannelSender = mock(SmsChannelSender.class);
        analogChannelSender = mock(AnalogChannelSender.class);
        channelSenderFactory = new ChannelSenderFactory(ioChannelSender, emailChannelSender, pecChannelSender, smsChannelSender,analogChannelSender);    
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

    @Test
    void shouldReturnSmsChannelSender_whenChannelIsSms() {
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.SMS);

        assertSame(smsChannelSender, result);
    }
    
  @Test
    void shouldReturnAnalogChannelSender_whenChannelIsAnalog() {
        ChannelSender result = channelSenderFactory.getChannelSender(ChannelType.ANALOG);

        assertSame(analogChannelSender, result);
    }
}