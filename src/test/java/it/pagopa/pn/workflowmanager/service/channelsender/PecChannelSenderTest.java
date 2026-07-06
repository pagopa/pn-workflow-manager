package it.pagopa.pn.workflowmanager.service.channelsender;

import it.pagopa.pn.workflowmanager.action.start_workflow.PecChannelSender;
import it.pagopa.pn.workflowmanager.action.utils.AttachmentType;
import it.pagopa.pn.workflowmanager.action.utils.AttachmentUtils;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import it.pagopa.pn.workflowmanager.utils.SendAttachmentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecChannelSenderTest {

    @Mock
    private TemplateGeneratorService templateGeneratorService;
    @Mock
    private AttachmentUtils attachmentUtils;
    @Mock
    private PnExternalChannelsClient pnExternalChannelsClient;
    @Mock
    private ChannelSenderUtils channelSenderUtils;
    @Mock
    private WorkflowUtils workflowUtils;

    @InjectMocks
    private PecChannelSender pecChannelSender;

    private NotificationInt notification;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .email("destinatario@pec.it")
                .digitalDomicile(it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt.builder()
                        .address("domicilio@pec.it")
                        .build())
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("Oggetto PEC")
                                .longBody("Corpo")
                                .language("it")
                                .build())
                        .build())
                .build();

        notification = NotificationInt.builder()
                .iun("IUN123")
                .sentAt(Instant.parse("2026-07-02T10:00:00Z"))
                .sender(NotificationSenderInt.builder().paDenomination("PA").build())
                .documents(List.of(NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder().key("doc1").build())
                        .build()))
                .recipients(List.of(recipient))
                .build();

        campaign = Campaign.builder()
                .campaignId("CMP001")
                .workflow(List.of(
                        WorkFlowEntity.builder()
                                .channel(ChannelType.PEC)
                                .recipientType(Set.of(RecipientTypeInt.PF))
                                .includeAttachment(true)
                                .timeout(Duration.ofMinutes(15))
                                .build()
                ))
                .build();
    }

    @Test
    void send_shouldInvokeDependenciesAndPersistTimelineAndScheduleTimeout() {
        SendAttachmentMode sendAttachmentMode = new SendAttachmentMode(Set.of(AttachmentType.DOCUMENTS));
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();

        when(templateGeneratorService.generatePecTemplate(notification, recipient, false))
                .thenReturn("<html>PEC</html>");
        when(attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.PEC)).thenReturn(sendAttachmentMode);
        when(attachmentUtils.retrieveAttachments(notification, 0, sendAttachmentMode, false))
                .thenReturn(List.of("safestorage://doc1"));

        pecChannelSender.send(notification, campaign, 0, 0, ChannelType.PEC);

        ArgumentCaptor<String> requestIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(pnExternalChannelsClient).sendNotificationPEC(
                requestIdCaptor.capture(),
                eq("<html>PEC</html>"),
                eq(notification),
                eq(recipient),
                eq(recipient.getDigitalDomicile()),
                eq(List.of("safestorage://doc1"))
        );

        String timelineId = requestIdCaptor.getValue();
        assertTrue(timelineId.contains("IUN123"));

        ArgumentCaptor<InformalDigitalAddressInt> digitalAddressCaptor = ArgumentCaptor.forClass(InformalDigitalAddressInt.class);
        verify(channelSenderUtils).saveSendDigitalMessageElement(
                eq(notification),
                eq(timelineId),
                eq(0),
                digitalAddressCaptor.capture(),
                eq(DigitalChannelsInt.PEC),
                isNull()
        );

        InformalDigitalAddressInt capturedDigitalAddress = digitalAddressCaptor.getValue();
        assertEquals("destinatario@pec.it", capturedDigitalAddress.getAddress());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC, capturedDigitalAddress.getType());

        verify(workflowUtils).scheduleTimeoutForCurrentChannel("IUN123", 0, 0, campaign, ChannelType.PEC);
    }
}

