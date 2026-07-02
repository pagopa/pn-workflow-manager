package it.pagopa.pn.workflowmanager.action.start_workflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector.IoConnectorClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IoChannelSenderTest {
    private static final String expectedAuditLogMessage = "Sending message for notification {} to recipient {} with requestId {}";

    @Mock
    private TemplateGeneratorService templateGeneratorService;

    @Mock
    private IoConnectorClient ioConnectorClient;

    @Mock
    private ChannelSenderUtils channelSenderUtils;

    @Mock
    private WorkflowUtils workflowUtils;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private IoChannelSender ioChannelSender;

    @Test
    void shouldSendMessagePersistDigitalEventAndScheduleTimeoutWhenInputIsValid() {
        String iun = "IUN_123";
        NotificationInt notification = mock(NotificationInt.class);
        NotificationRecipientInt recipient0 = mock(NotificationRecipientInt.class);
        Campaign campaign = mock(Campaign.class);

        when(notification.getIun()).thenReturn(iun);
        when(notification.getRecipients()).thenReturn(List.of(recipient0));
        when(recipient0.getTaxId()).thenReturn("TAXID_1");

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(iun,0, ChannelType.IO);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);
        when(auditLogService.buildAuditLogEvent(iun, 0, PnAuditLogEventType.AUD_COM_SEND_IO, expectedAuditLogMessage, iun, 0, expectedRequestId))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(any())).thenReturn(auditLogEvent);

        when(templateGeneratorService.generateIoMessageTemplate(notification, recipient0, false)).thenReturn("markdown-content");

        ioChannelSender.send(notification, campaign,0,0, ChannelType.IO);

        ArgumentCaptor<IoMessageRequest> requestCaptor = ArgumentCaptor.forClass(IoMessageRequest.class);
        verify(ioConnectorClient).sendMessage(requestCaptor.capture());

        IoMessageRequest sentRequest = requestCaptor.getValue();
        assertEquals(expectedRequestId, sentRequest.getRequestId());
        assertEquals("markdown-content", sentRequest.getMarkdown());
        assertSame(notification, sentRequest.getNotificationInt());
        assertSame(recipient0, sentRequest.getNotificationRecipientInt());
        assertSame(campaign, sentRequest.getCampaign());

        verify(channelSenderUtils).saveSendDigitalMessageElement(
                eq(notification),
                eq(expectedRequestId),
                eq(0),
                any(InformalDigitalAddressInt.class),
                eq(DigitalChannelsInt.APPIO),
                isNull()
        );
        verify(workflowUtils).scheduleTimeoutForCurrentChannel(iun, 0, 0, campaign, ChannelType.IO);
        verify(auditLogService).buildAuditLogEvent(iun, 0, PnAuditLogEventType.AUD_COM_SEND_IO, "Sending message for notification {} to recipient {} with requestId {}", iun, 0, expectedRequestId);
        verify(auditLogEvent).generateSuccess(any());
    }

    @Test
    void shouldThrowIndexOutOfBoundsWhenRecipientIndexDoesNotExist() {
        // Scenario impossibile in teoria.
        NotificationInt notification = mock(NotificationInt.class);
        NotificationRecipientInt recipient = mock(NotificationRecipientInt.class);
        Campaign campaign = mock(Campaign.class);

        when(notification.getIun()).thenReturn("IUN_789");
        when(notification.getRecipients()).thenReturn(List.of(recipient));

        assertThrows(IndexOutOfBoundsException.class, () -> ioChannelSender.send(notification, campaign,3,0, ChannelType.IO));

        verifyNoInteractions(templateGeneratorService, ioConnectorClient, channelSenderUtils, workflowUtils, auditLogService);
    }

    @Test
    void shouldThrowPnInternalExceptionWhenIoConnectorClientFails() {
        String iun = "IUN_456";
        NotificationInt notification = mock(NotificationInt.class);
        NotificationRecipientInt recipient0 = mock(NotificationRecipientInt.class);
        Campaign campaign = mock(Campaign.class);

        when(notification.getIun()).thenReturn(iun);
        when(notification.getRecipients()).thenReturn(List.of(recipient0));

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(iun,0, ChannelType.IO);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogService.buildAuditLogEvent(iun, 0, PnAuditLogEventType.AUD_COM_SEND_IO, expectedAuditLogMessage, iun, 0, expectedRequestId))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateFailure(anyString(), any())).thenReturn(auditLogEvent);

        when(templateGeneratorService.generateIoMessageTemplate(notification, recipient0, false)).thenReturn("markdown-content");

        doThrow(new RuntimeException("IO Connector failure")).when(ioConnectorClient).sendMessage(any(IoMessageRequest.class));

        assertThrows(PnInternalException.class, () -> ioChannelSender.send(notification, campaign,0,0, ChannelType.IO));

        verify(auditLogService).buildAuditLogEvent(eq(iun), eq(0), eq(PnAuditLogEventType.AUD_COM_SEND_IO), anyString(), eq(iun), eq(0), anyString());
        verify(auditLogEvent).generateFailure(eq("Error sending message"), any(RuntimeException.class));
    }
}