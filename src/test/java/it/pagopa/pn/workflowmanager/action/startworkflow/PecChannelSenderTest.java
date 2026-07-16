package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
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
    private PnExternalChannelsClient pnExternalChannelsClient;
    @Mock
    private ChannelSenderUtils channelSenderUtils;
    @Mock
    private WorkflowUtils workflowUtils;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PecChannelSender pecChannelSender;

    private NotificationInt notification;
    private Campaign campaign;
    private static final String IUN = "IUN123";

    @BeforeEach
    void setUp() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .email("destinatario@email.it")
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
                .iun(IUN)
                .sentAt(Instant.parse("2026-07-02T10:00:00Z"))
                .sender(NotificationSenderInt.builder().paDenomination("PA").build())
                .documents(List.of(NotificationDocumentInt.builder()
                        .ref(NotificationDocumentInt.Ref.builder().build())
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
    void shouldReturnCorrectChannelType() {
        assertEquals(ChannelType.PEC, pecChannelSender.getChannelType());
    }

    @Test
    void send_shouldInvokeDependenciesAndPersistTimelineAndScheduleTimeout() {
        int recIndex = 0;
        int currentStep = 0;
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.PEC);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_PEC),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);
        when(templateGeneratorService.generatePecBodyTemplate(notification, recipient, campaign))
                .thenReturn("<html>PEC</html>");
        when(templateGeneratorService.generatePecSubjectTemplate(notification, recipient))
                .thenReturn("Oggetto PEC");
        when(channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, ChannelType.PEC))
                .thenReturn(List.of("safestorage://doc1"));

        pecChannelSender.send(notification, campaign, recIndex, currentStep);

        ArgumentCaptor<String> requestIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(pnExternalChannelsClient).sendNotificationPEC(
                requestIdCaptor.capture(),
                eq("<html>PEC</html>"),
                eq("Oggetto PEC"),
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
                eq(recIndex),
                digitalAddressCaptor.capture(),
                eq(DigitalChannelsInt.PEC),
                isNull()
        );

        InformalDigitalAddressInt capturedDigitalAddress = digitalAddressCaptor.getValue();
        assertEquals("domicilio@pec.it", capturedDigitalAddress.getAddress());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC, capturedDigitalAddress.getType());

        verify(workflowUtils).scheduleTimeoutForCurrentChannel("IUN123", recIndex, campaign, ChannelType.PEC);
        verify(auditLogEvent).generateSuccess("Pec sent successfully");
    }

    @Test
    void send_shouldPrintAuditLogFailureIfSomethingFails() {
        int recIndex = 0;
        int currentStep = 0;
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.PEC);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_PEC),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateFailure(anyString(), any(RuntimeException.class))).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);
        when(templateGeneratorService.generatePecBodyTemplate(notification, recipient, campaign))
                .thenThrow(new RuntimeException("Template generation failed"));

        assertThrows(PnInternalException.class, () -> pecChannelSender.send(notification, campaign, recIndex, currentStep));

        verify(pnExternalChannelsClient, never()).sendNotificationPEC(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(channelSenderUtils, never()).saveSendDigitalMessageElement(
                any(),
                any(),
                anyInt(),
                any(),
                any(),
                any()
        );

        verify(workflowUtils, never()).scheduleTimeoutForCurrentChannel(any(), anyInt(), any(), any());
        verify(auditLogEvent).generateFailure(eq("Error sending pec"), any(RuntimeException.class));
    }
}

