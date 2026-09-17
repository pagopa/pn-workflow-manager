package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

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
        doReturn(null).when(pnExternalChannelsClient).sendNotificationSMS(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        assertEquals(CourtesySendOutcome.SENT, outcome);
    }
}
