package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.utils.NotificationUtils;
import it.pagopa.pn.workflowmanager.utils.PnSendMode;
import it.pagopa.pn.workflowmanager.utils.PnSendModeUtils;
import it.pagopa.pn.workflowmanager.utils.SendAttachmentMode;
import it.pagopa.pn.workflowmanager.utils.TimelineUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND;

@Component
@CustomLog
@AllArgsConstructor
public class AttachmentUtils {
    private final PnSendModeUtils pnSendModeUtils;
    private final NotificationUtils notificationUtils;
    private final TimelineUtils timelineUtils;

    /**
     * Recupera gli allegati della notifica, in base al tipo di invio
     *
     * @param notification notifica
     * @param recIndex indice destinatario
     * @return lista id allegati
     */
    public List<String> retrieveAttachments(
            NotificationInt notification,
            Integer recIndex,
            SendAttachmentMode sendAttachmentMode,
            Boolean formatWithDocTag
    ) {

        log.info("retrieveAttachments iun={} recIndex={} sendAttachmentMode={}",
                notification.getIun(), recIndex, sendAttachmentMode);

        List<String> attachments = new ArrayList<>();

        // COVERPAGE è sempre inclusa se presente nel mode
        if (sendAttachmentMode.includes(AttachmentType.COVERPAGE)) {
            String coverpageFileKey = timelineUtils.retrieveCoverpageFileKey(notification.getIun(), recIndex);
            attachments.add(formatWithDocTag(
                    FileUtils.getKeyWithStoragePrefix(coverpageFileKey),
                    FileTagEnumInt.COVERPAGE,
                    formatWithDocTag
            ));
        }

        if (sendAttachmentMode.includes(AttachmentType.DOCUMENTS)) {
            attachments.addAll(getNotificationAttachments(notification, formatWithDocTag));
        }

        if (sendAttachmentMode.includes(AttachmentType.PAYMENTS)) {
            NotificationRecipientInt recipient = notificationUtils.getRecipientFromIndex(notification, recIndex);
            attachments.addAll(getNotificationPagoPaPayments(recipient, formatWithDocTag));
        }

        log.info("retrieveAttachments iun={} recIndex={} attachmentsToSend={}", notification.getIun(), recIndex, attachments);
        return attachments;
    }

    private List<String> getNotificationAttachments(NotificationInt notification, Boolean formatWithDocTag) {
        return notification.getDocuments().stream()
                .map(attachment -> FileUtils.getKeyWithStoragePrefix(attachment.getRef().getKey()))
                .map(u -> formatWithDocTag(u, FileTagEnumInt.DOCUMENT, formatWithDocTag))
                .toList();
    }

    @NotNull
    private List<String> getNotificationPagoPaPayments(NotificationRecipientInt recipient, Boolean formatWithDocTag) {
        return recipient.getPayments().stream()
                .filter(notificationPaymentInfoIntV2 -> notificationPaymentInfoIntV2.getPagoPA() != null && notificationPaymentInfoIntV2.getPagoPA().getAttachment() != null)
                .map(payment -> payment.getPagoPA().getAttachment())
                .map(attachment -> FileUtils.getKeyWithStoragePrefix(attachment.getRef().getKey()))
                .map(u -> formatWithDocTag(u, FileTagEnumInt.ATTACHMENT_PAGOPA, formatWithDocTag))
                .toList();
    }

    private String formatWithDocTag(String uri, FileTagEnumInt docTag, Boolean formatWithDocTag){
        if (Boolean.TRUE.equals(formatWithDocTag)){
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);
            uriBuilder.queryParam("docTag",docTag.getValue());
            return uriBuilder.toUriString();
        }
        return uri;
    }

    public SendAttachmentMode retrieveAttachmentTypesToSend(NotificationInt notification, ChannelType channel) {
        PnSendMode pnSendMode = pnSendModeUtils.getPnSendMode(notification.getSentAt());

        if(pnSendMode != null){
            return switch (channel) {
                case PEC -> pnSendMode.getPecSendAttachmentMode();
                case EMAIL -> pnSendMode.getEmailSendAttachmentMode();
                case ANALOG -> pnSendMode.getSimpleRegisteredLetterSendAttachmentMode();
                default -> {
                    String msg = String.format("Channel %s not supported for Send Attachment configuration date=%s - iun=%s sentAt", channel, notification.getSentAt(),  notification.getIun());
                    log.error(msg);
                    throw new PnInternalException(msg, ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND);
                }
            };
        }else {
            String msg = String.format("There isn't correct Send Analog configuration date=%s - iun=%s sentAt", notification.getSentAt(),  notification.getIun());
            log.error(msg);
            throw new PnInternalException(msg, ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND);
        }
    }
}
