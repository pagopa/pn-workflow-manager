package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunicationBody;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunicationSender;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.SharedInformalCommunicationRecipient;
import org.springframework.util.CollectionUtils;

public class TemplateEngineMapper {
    private TemplateEngineMapper() {
    }

    public static InformalCommunication mapToInformalCommunication(NotificationInt notification, NotificationRecipientInt recipient, boolean isIoUser) {
        return new InformalCommunication()
                .iun(notification.getIun())
                .subject(recipient.getMessage().getPrimaryMessage().getSubject())
                .hasAttachment(!CollectionUtils.isEmpty(notification.getDocuments()))
                .hasPayment(!CollectionUtils.isEmpty(recipient.getPayments()))
                .body(mapToInformalCommunicationBody(recipient.getMessage()))
                .sender(mapToInformalCommunicationSender(notification.getSender()))
                .recipient(mapToInformalCommunicationRecipient(recipient, isIoUser));
    }

    private static InformalCommunicationBody mapToInformalCommunicationBody(NotificationMessageInt message) {
        return new InformalCommunicationBody()
                .primaryContent(message.getPrimaryMessage().getLongBody())
                .secondaryContent(message.getAdditionalMessage() != null ? message.getAdditionalMessage().getLongBody() : null);
    }

    private static InformalCommunicationSender mapToInformalCommunicationSender(NotificationSenderInt sender) {
        return new InformalCommunicationSender()
                .paDenomination(sender.getPaDenomination());
    }

    private static SharedInformalCommunicationRecipient mapToInformalCommunicationRecipient(NotificationRecipientInt recipient, boolean isIoUser) {
        return new SharedInformalCommunicationRecipient()
                .taxId(recipient.getTaxId())
                .denomination(recipient.getDenomination())
                .recipientType(SharedInformalCommunicationRecipient.RecipientTypeEnum.fromValue(recipient.getRecipientType().name()))
                .isIoUser(isIoUser);
    }


}
