package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
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
    void sendReturnsSentWhenExternalCallSucceeds() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder().iun("IUN-1").recipients(List.of(recipient)).build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address("123")
                .build();
        when(templateGeneratorService.generateSmsTemplate(notification, recipient)).thenReturn("subject");
        when(auditLogService.buildAuditLogEvent("IUN-1", 0, PnAuditLogEventType.AUD_COM_SEND_SMS_COURTESY, "Sending courtesy email for notification {} to recipient {} with requestId {}", "IUN-1", 0, "SEND_COURTESY_MESSAGE.IUN_IUN-1.RECINDEX_0.COURTESYADDRESSTYPE_SMS")).thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_COM_SEND_SMS_COURTESY, null, null));

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        verify(pnExternalChannelsClient).sendNotificationSMS(any(), any(), any(), any());
        assertEquals(CourtesySendOutcome.SENT, outcome);
    }
}
