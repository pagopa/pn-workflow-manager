package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.*;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import org.springframework.util.CollectionUtils;

public class TemplateEngineMapper {
    private TemplateEngineMapper() {
    }

    public static InformalCommunication mapToInformalCommunication(NotificationInt notification, NotificationRecipientInt recipient, Campaign campaign) {
        return new InformalCommunication()
                .iun(notification.getIun())
                .subject(recipient.getMessage().getPrimaryMessage().getSubject())
                .hasAttachment(!CollectionUtils.isEmpty(notification.getDocuments()))
                .hasPayment(!CollectionUtils.isEmpty(recipient.getPayments()))
                .body(mapToInformalCommunicationBody(recipient.getMessage()))
                .sender(mapToInformalCommunicationSender(notification.getSender(), campaign))
                .recipient(mapToInformalCommunicationRecipient(recipient));
                //TODO: .checkoutUrl();
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
                .recipientType(SharedInformalCommunicationRecipient.RecipientTypeEnum.fromValue(recipient.getRecipientType().name()));
    }

    public static InformalEmailCommunicationSubject mapToInformalEmailCommunicationSubject(NotificationInt notification, NotificationRecipientInt recipient) {
        return new InformalEmailCommunicationSubject()
                .subject(recipient.getMessage().getPrimaryMessage().getSubject())
                .recipientDenomination(recipient.getDenomination())
                .senderDenomination(notification.getSender().getPaDenomination());
    }
    public static InformalSmsCommunication mapToInformalSmsCommunication(NotificationInt notification) {
        return new InformalSmsCommunication()
                .senderPaDenomination(notification.getSender().getPaDenomination());
    }


}
