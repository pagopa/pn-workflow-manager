package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.AttachmentUtils;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel.PaperMessagesClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Service
@Slf4j
@AllArgsConstructor
public class PaperChannelServiceImpl implements PaperChannelService {
    private static final Integer FIRST_ATTEMPT = 0;

    private final PaperMessagesClient paperMessagesClient;
    private final AttachmentUtils attachmentUtils;
    private final AuditLogService auditLogService;
    private final ChannelSenderUtils channelSenderUtils;

    @Override
    public void prepareSimpleRegisteredLetter(NotificationInt notification, Integer recIndex, String coverpageFileKey) {
        log.info("Start prepareSimpleRegisteredLetter - iun={} recIndex={} coverpageFileKey={}", notification.getIun(), recIndex, coverpageFileKey);
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        PhysicalAddressInt physicalAddressInt = recipient.getPhysicalAddress();
        String requestId = ChannelSenderUtils.buildPrepareAnalogDeliveryTimelineElementId(recIndex, notification.getIun(), FIRST_ATTEMPT);//ToDo: Per l'invio di una notifica bonaria si presuppone che ci sia un solo invio
        PaperChannelPrepareRequest paperChannelPrepareRequest = PaperChannelPrepareRequest.builder()
                .requestId(requestId)
                .notificationInt(notification)
                .recipientInt(recipient)
                .paAddress(physicalAddressInt)
                .analogType(PhysicalAddressInt.ANALOG_TYPE.SIMPLE_REGISTERED_LETTER)
                .attachments(retrieveAttachmentsToSend(notification, recIndex))
                .build();
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);
        try {
            log.info("Preparing simple registered letter - iun={} recIndex={} requestId={}", notification.getIun(), recIndex, requestId);
            paperMessagesClient.prepare(paperChannelPrepareRequest);
            channelSenderUtils.savePrepareAnalogDeliveryElement(
                    recIndex,
                    notification,
                    requestId,
                    null,//Todo: Per l'invio di una notifica bonaria il dato serviceLevel è assente
                    FIRST_ATTEMPT,
                    null,//ToDo: Per l'invio di una notifica bonaria si presuppone che ci sia un solo invio, quindi verrà valorizzato solo con un secondo invio
                    physicalAddressInt);
            auditLogEvent.generateSuccess("analog informal notification sent successfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending analog informal notification", e).log();
            throw new PnInternalException(
                    "Error sending analog informal notification for notification " + notification.getIun() + " to recipientIdx " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }

    private List<String> retrieveAttachmentsToSend(NotificationInt notification, int recIndex) {
        return attachmentUtils.retrieveAttachments(
                notification, recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.ANALOG),
                false
        );
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending analog informal notification {} to recipientIdx {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_PD_PREPARE, msg, iun, recIndex, requestId);
    }
}
