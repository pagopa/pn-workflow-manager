package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
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
import static org.mockito.Mockito.when;

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
    void sendReturnsSentWhenExternalCallSucceeds() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().internalId("r1").build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-1")
                .campaignId("c1")
                .recipients(List.of(recipient))
                .build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("a@b.it")
                .build();
        Campaign campaign = Campaign.builder().build();
        when(campaignService.getCampaignByCampaignIdAndSenderId("c1", null)).thenReturn(campaign);
        when(templateGeneratorService.generateEmailSubjectTemplate(notification, recipient)).thenReturn("subject");
        when(templateGeneratorService.generateEmailBodyTemplate(notification, recipient, campaign)).thenReturn("body");
        when(configs.getEmailCourtesyRequiresAttachments()).thenReturn(false);
        org.mockito.Mockito.doReturn(null).when(pnExternalChannelsClient).sendNotificationEMAIL(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(notification), org.mockito.ArgumentMatchers.eq(recipient), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any());

        CourtesySendOutcome outcome = sender.send(notification, address, 0);

        assertEquals(CourtesySendOutcome.SENT, outcome);
    }
}
