package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateEngineMapperTest {
    @Test
    void shouldMapToInformalCommunicationWhenNoDocumentsAndNoPayments() {
        NotificationInt notification = buildNotification(List.of());
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of());

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, true);

        assertMapping(result, false, false, null);
    }

    @Test
    void shouldMapToInformalCommunicationWhenDocumentsAndPaymentsArePresent() {
        NotificationDocumentInt document = NotificationDocumentInt.builder().build();
        NotificationPaymentInfoInt payment = NotificationPaymentInfoInt.builder().build();
        NotificationInt notification = buildNotification(List.of(document));
        NotificationRecipientInt recipient = buildNotificationRecipient(List.of(payment));

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, true);

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

        InformalCommunication result = TemplateEngineMapper.mapToInformalCommunication(notification, recipient, true);

        assertMapping(result, false, false, "Secondary content");
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

    private void assertMapping(InformalCommunication result, boolean expectedHasAttachment, boolean expectedHasPayment, String expectedSecondaryContent) {
        // fixed values
        assertEquals("IUN_001", result.getIun());
        assertEquals("subject", result.getSubject());
        assertEquals("senderDenomination", result.getSender().getPaDenomination());
        assertEquals("recipientTaxId", result.getRecipient().getTaxId());
        assertEquals("recipientDenomination", result.getRecipient().getDenomination());
        assertTrue(result.getRecipient().getIsIoUser());

        // dynamic values
        assertEquals(expectedHasAttachment, result.getHasAttachment());
        assertEquals(expectedHasPayment, result.getHasPayment());
        assertEquals(expectedSecondaryContent, result.getBody().getSecondaryContent());
    }

}