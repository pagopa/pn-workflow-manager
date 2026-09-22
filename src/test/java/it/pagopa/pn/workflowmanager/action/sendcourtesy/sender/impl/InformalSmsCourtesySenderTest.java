package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ExternalChannelEventType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformalSmsCourtesySenderTest {

    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PnExternalChannelsClient pnExternalChannelsClient;
    @Mock
    private TemplateGeneratorService templateGeneratorService;
    @Mock
    private CourtesyRetryableErrorClassifier retryableErrorClassifier;
    @Mock
    private CourtesyMessageUtils courtesyMessageUtils;

    @InjectMocks
    private InformalSmsCourtesySender sender;

    @Test
    void getMetadataIsSmsInformal() {
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS, sender.getCourtesyAddressType());
    }

    @Test
    void getCommunicationTypeIsInformal() {
        assertEquals(CommunicationType.INFORMAL, sender.getCommunicationType());
    }

    @Test
    void sendReturnsSentWhenExternalCallSucceeds() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder().iun("IUN-1").recipients(List.of(recipient)).build();
        String receiverAddress = "3339232423";
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address(receiverAddress)
                .build();
        String smsBody = "template";
        when(templateGeneratorService.generateCourtesySmsTemplate(notification, recipient)).thenReturn(smsBody);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogService.buildAuditLogEvent("IUN-1", 0, PnAuditLogEventType.AUD_COM_SEND_SMS_COURTESY, "Sending courtesy SMS for notification {} to recipient {} with requestId {}", "IUN-1", 0, "SEND_COURTESY_MESSAGE.IUN_IUN-1.RECINDEX_0.COURTESYADDRESSTYPE_SMS")).thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString(), anyString(), anyInt())).thenReturn(auditLogEvent);


        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        verify(pnExternalChannelsClient).sendNotificationSMS(any(), eq(smsBody), eq(receiverAddress), eq(ExternalChannelEventType.COURTESY));
        assertEquals(CourtesySendOutcome.SENT, outcome);
        verify(auditLogEvent).generateSuccess("Courtesy SMS sent successfully - iun={} id={}", "IUN-1", 0);
        verify(courtesyMessageUtils).addSendCourtesyMessageToTimeline(eq(notification), eq(0), eq(address), any(), any(), isNull());
    }

    @Test
    void sendReturnsRetryableErrorWhenExternalCallFailsWithRetryableError() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder().iun("IUN-1").recipients(List.of(recipient)).build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address("123")
                .build();
        when(templateGeneratorService.generateCourtesySmsTemplate(notification, recipient)).thenReturn("subject");
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogService.buildAuditLogEvent("IUN-1", 0, PnAuditLogEventType.AUD_COM_SEND_SMS_COURTESY, "Sending courtesy SMS for notification {} to recipient {} with requestId {}", "IUN-1", 0, "SEND_COURTESY_MESSAGE.IUN_IUN-1.RECINDEX_0.COURTESYADDRESSTYPE_SMS")).thenReturn(auditLogEvent);
        Exception exception = new RuntimeException("Transport error");
        doThrow(exception).when(pnExternalChannelsClient).sendNotificationSMS(any(), any(), any(), any());
        when(retryableErrorClassifier.isRetryableTransportError(eq(address.getType()), any())).thenReturn(true);
        when(auditLogEvent.generateFailure(anyString(), any(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.class), anyBoolean(), anyString(), anyInt(), any(Exception.class))).thenReturn(auditLogEvent);

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        assertEquals(CourtesySendOutcome.RETRYABLE_ERROR, outcome);
        verify(auditLogEvent).generateFailure("Error sending courtesy message on channel={} retryable={} - iun={} id={}", address.getType(), true, "IUN-1", 0, exception);
    }
}
