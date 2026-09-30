package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.*;
import org.springframework.util.CollectionUtils;
import java.util.List;

public class TemplateEngineMapper {
    private TemplateEngineMapper() {
    }

    public static InformalCommunication mapToInformalCommunication(NotificationInt notification, NotificationRecipientInt recipient, Campaign campaign) {
        return new InformalCommunication()
                .iun(notification.getIun())
                .subject(recipient.getMessage().getPrimaryMessage().getSubject())
                .hasAttachment(!CollectionUtils.isEmpty(notification.getDocuments()))
                .hasPayment(!CollectionUtils.isEmpty(recipient.getPayments()))
                .paymentHasAttachment(checkIfPaymentHasAttachment(recipient.getPayments()))
                .body(mapToInformalCommunicationBody(recipient.getMessage()))
                .sender(mapToInformalCommunicationSender(notification.getSender(), campaign))
                .recipient(mapToInformalCommunicationRecipient(recipient));
    }

    // Verifica presenza allegati controllando che tutta la lista abbia degli allegati presenti.
    // Per le notifiche bonarie è atteso esattamente 1 elemento
    private static boolean checkIfPaymentHasAttachment(List<NotificationPaymentInfoInt> payments) {
        return !CollectionUtils.isEmpty(payments)
                && payments.stream().allMatch(payment ->
                (payment.getPagoPA() != null
                        && payment.getPagoPA().getAttachment() != null));
    }

    private static InformalCommunicationBody mapToInformalCommunicationBody(NotificationMessageInt message) {
        return new InformalCommunicationBody()
                .primaryContent(message.getPrimaryMessage().getLongBody())
                .secondaryContent(message.getAdditionalMessage() != null ? message.getAdditionalMessage().getLongBody() : null);
    }

    private static InformalCommunicationSender mapToInformalCommunicationSender(NotificationSenderInt sender, Campaign campaign) {
        return new InformalCommunicationSender()
                .denomination(sender.getPaDenomination())
                .id(sender.getPaId())
                .service(campaign.getServiceName());
    }

    private static SharedInformalCommunicationRecipient mapToInformalCommunicationRecipient(NotificationRecipientInt recipient) {
        return new SharedInformalCommunicationRecipient()
                .taxId(recipient.getTaxId())
                .denomination(recipient.getDenomination())
                .recipientType(RecipientTypeEnum.fromValue(recipient.getRecipientType().name()));
    }

    public static InformalEmailCommunicationSubject mapToInformalEmailCommunicationSubject(NotificationInt notification, NotificationRecipientInt recipient) {
        return new InformalEmailCommunicationSubject()
                .subject(recipient.getMessage().getPrimaryMessage().getSubject())
                .recipientDenomination(recipient.getDenomination())
                .senderDenomination(notification.getSender().getPaDenomination());
    }
    public static InformalSmsCommunication mapToInformalSmsCommunication(NotificationInt notification, NotificationRecipientInt recipient) {
        return new InformalSmsCommunication()
                .recipientType(RecipientTypeEnum.fromValue(recipient.getRecipientType().name()))
                .senderPaDenomination(notification.getSender().getPaDenomination());
    }


}
