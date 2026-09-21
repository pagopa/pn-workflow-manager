package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ExternalChannelEventType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformalEmailCourtesySenderTest {

    @Mock private PnWorkflowManagerConfigs configs;
    @Mock private AuditLogService auditLogService;
    @Mock private PnExternalChannelsClient pnExternalChannelsClient;
    @Mock private TemplateGeneratorService templateGeneratorService;
    @Mock private CampaignService campaignService;
    @Mock private ChannelSenderUtils channelSenderUtils;
    @Mock private CourtesyRetryableErrorClassifier retryableErrorClassifier;
    @Mock private CourtesyMessageUtils courtesyMessageUtils;

    @InjectMocks
    private InformalEmailCourtesySender sender;

    @Test
    void getMetadataIsEmailInformal() {
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, sender.getCourtesyAddressType());
    }

    @Test
    void getCommunicationTypeIsInformal() {
        assertEquals(CommunicationType.INFORMAL, sender.getCommunicationType());
    }

    @Test
    void sendReturnsSentWhenExternalCallSucceeds() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-1")
                .campaignId("c1")
                .recipients(List.of(recipient))
                .sender(NotificationSenderInt.builder().paId(null).build())
                .build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("a@b.it")
                .build();
        Campaign campaign = Campaign.builder().build();
        when(campaignService.getCampaignByCampaignIdAndSenderId("c1", null)).thenReturn(campaign);
        when(templateGeneratorService.generateCourtesyEmailSubjectTemplate(notification, recipient)).thenReturn("subject");
        when(templateGeneratorService.generateCourtesyEmailBodyTemplate(notification, recipient, campaign)).thenReturn("body");
        when(configs.getEmailCourtesyRequiresAttachments()).thenReturn(true);
        when(channelSenderUtils.resolveAttachmentsForChannel(any(), anyInt(), any(), any())).thenReturn(List.of("attachment1"));
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogService.buildAuditLogEvent("IUN-1", 0, PnAuditLogEventType.AUD_COM_SEND_EMAIL_COURTESY, "Sending courtesy email for notification {} to recipient {} with requestId {}", "IUN-1", 0, "SEND_COURTESY_MESSAGE.IUN_IUN-1.RECINDEX_0.COURTESYADDRESSTYPE_EMAIL")).thenReturn(auditLogEvent);

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        verify(pnExternalChannelsClient).sendNotificationEMAIL(any(), any(), any(), any(), any(), any(), any(), eq(ExternalChannelEventType.COURTESY));
        verify(courtesyMessageUtils).addSendCourtesyMessageToTimeline(any(), any(), any(), any(), any(), any());
        verify(auditLogEvent).generateSuccess("Courtesy email sent successfully - iun={} id={}", "IUN-1", 0);
        assertEquals(CourtesySendOutcome.SENT, outcome);
    }

    @Test
    void sendReturnsRetryableErrorWhenExternalCallFailsWithRetryableError() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-1")
                .campaignId("c1")
                .recipients(List.of(recipient))
                .sender(NotificationSenderInt.builder().paId(null).build())
                .build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("test@address.it")
                .build();

        when(campaignService.getCampaignByCampaignIdAndSenderId("c1", null)).thenReturn(Campaign.builder().build());
        when(templateGeneratorService.generateCourtesyEmailSubjectTemplate(notification, recipient)).thenReturn("subject");
        when(templateGeneratorService.generateCourtesyEmailBodyTemplate(notification, recipient, Campaign.builder().build())).thenReturn("body");
        when(configs.getEmailCourtesyRequiresAttachments()).thenReturn(false);
        when(retryableErrorClassifier.isRetryableTransportError(any(), any())).thenReturn(true);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        when(auditLogService.buildAuditLogEvent("IUN-1", 0, PnAuditLogEventType.AUD_COM_SEND_EMAIL_COURTESY, "Sending courtesy email for notification {} to recipient {} with requestId {}", "IUN-1", 0, "SEND_COURTESY_MESSAGE.IUN_IUN-1.RECINDEX_0.COURTESYADDRESSTYPE_EMAIL")).thenReturn(auditLogEvent);

        doThrow(new RuntimeException("Simulated transport error")).when(pnExternalChannelsClient).sendNotificationEMAIL(any(), any(), any(), any(), any(), any(), any(), any());

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        assertEquals(CourtesySendOutcome.RETRYABLE_ERROR, outcome);
        verify(auditLogEvent).generateFailure(eq("Error sending courtesy message on channel={} retryable={} - iun={} id={}"), eq(address.getType()), eq(true), eq("IUN-1"), eq(0), any());
    }

}
