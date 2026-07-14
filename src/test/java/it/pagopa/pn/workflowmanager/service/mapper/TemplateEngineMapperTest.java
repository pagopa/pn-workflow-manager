package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.CampaignStatus;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalEmailCommunicationSubject;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.RecipientTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEngineMapperTest {
    @Test
    void shouldMapToInformalCommunicationWhenNoDocumentsAndNoPayments() {
        NotificationInt notification = buildNotification(List.of());
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of());
        Campaign campaign = buildCampaign();

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, campaign);

        assertMapping(result, false, false, null);
    }

    @Test
    void shouldMapToInformalCommunicationWhenDocumentsAndPaymentsArePresent() {
        NotificationDocumentInt document = NotificationDocumentInt.builder().build();
        NotificationPaymentInfoInt payment = NotificationPaymentInfoInt.builder().build();
        NotificationInt notification = buildNotification(List.of(document));
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of(payment));
        Campaign campaign = buildCampaign();

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, campaign);

        assertMapping(result, true, true, null);
    }

    @Test
    void shouldMapToInformalCommunicationWhenAdditionalMessageIsPresent() {
        NotificationInt notification = buildNotification(List.of());
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of());
        recipient.getMessage().setAdditionalMessage(
                LocalizedMessageInt.builder()
                        .longBody("Secondary content")
                        .subject("Secondary subject")
                        .language("DE")
                        .build()
        );
        Campaign campaign = buildCampaign();

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, campaign);

        assertMapping(result, false, false, "Secondary content");
    }

    @Test
    void shouldMapToInformalEmailCommunicationSubject() {
        NotificationInt notification = buildNotification(List.of());
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of());
        recipient.getMessage().setAdditionalMessage(
                LocalizedMessageInt.builder()
                        .longBody("Secondary content")
                        .subject("Secondary subject")
                        .language("DE")
                        .build()
        );

        InformalEmailCommunicationSubject result = TemplateEngineMapper.mapToInformalEmailCommunicationSubject(notification, recipient);

        assertEquals("subject", result.getSubject());
        assertEquals("recipientDenomination", result.getRecipientDenomination());
        assertEquals("senderDenomination", result.getSenderDenomination());
    }

    @Test
    void shouldMapToInformalSmsCommunication() {
        NotificationInt notification = buildNotification(List.of());
        var result = TemplateEngineMapper.mapToInformalSmsCommunication(notification);

        assertEquals("senderDenomination", result.getSenderPaDenomination());
    }

    private NotificationInt buildNotification(List<NotificationDocumentInt> documents) {
        return NotificationInt.builder()
                .iun("IUN_001")
                .sender(NotificationSenderInt.builder()
                        .paTaxId("senderTaxId")
                        .paDenomination("senderDenomination")
                        .paId("senderId")
                        .build())
                .documents(documents)
                .build();
    }

    private NotificationRecipientInt buildNotificationRecipient(List<NotificationPaymentInfoInt> payments) {
        return NotificationRecipientInt.builder()
                .taxId("recipientTaxId")
                .denomination("recipientDenomination")
                .recipientType(RecipientTypeInt.PF)
                .additionalLanguages(Collections.emptyList())
                .payments(payments)
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .language("IT")
                                .longBody("longBody")
                                .build())
                        .build())
                .build();
    }

    private Campaign buildCampaign() {
        return Campaign.builder()
                .campaignId("campaignId")
                .senderId("senderId")
                .title("title")
                .descriptionScope("descriptionScope")
                .status(CampaignStatus.IN_PROGRESS)
                .serviceId("serviceId")
                .serviceName("serviceName")
                .workflow(List.of())
                .build();
    }

    private void assertMapping(InformalCommunication result, boolean expectedHasAttachment, boolean expectedHasPayment, String expectedSecondaryContent) {
        // fixed values
        assertEquals("IUN_001", result.getIun());
        assertEquals("subject", result.getSubject());
        assertEquals("senderDenomination", result.getSender().getDenomination());
        assertEquals("senderId", result.getSender().getId());
        assertEquals("serviceName", result.getSender().getService());
        assertEquals("recipientTaxId", result.getRecipient().getTaxId());
        assertEquals("recipientDenomination", result.getRecipient().getDenomination());
        assertEquals(RecipientTypeEnum.PF, result.getRecipient().getRecipientType());


        // dynamic values
        assertEquals(expectedHasAttachment, result.getHasAttachment());
        assertEquals(expectedHasPayment, result.getHasPayment());
        assertEquals(expectedSecondaryContent, result.getBody().getSecondaryContent());
    }

}