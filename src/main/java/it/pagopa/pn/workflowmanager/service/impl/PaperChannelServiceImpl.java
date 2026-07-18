package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelSendRequest;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel.PaperMessagesClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import static it.pagopa.pn.workflowmanager.action.utils.NotificationUtils.getRecipientFromIndex;
import static it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils.buildAnalogDto;
import static it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils.getAttachments;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Service
@Slf4j
@AllArgsConstructor
public class PaperChannelServiceImpl implements PaperChannelService {
    private static final Integer FIRST_ATTEMPT = 0;

    private final PaperMessagesClient paperMessagesClient;
    private final AuditLogService auditLogService;
    private final PaperChannelUtils paperChannelUtils;
    private final ChannelSenderUtils channelSenderUtils;

    @Override
    public void prepareSimpleRegisteredLetter(NotificationInt notification, Integer recIndex) {
        log.info("Start prepareSimpleRegisteredLetter - iun={} recIndex={}", notification.getIun(), recIndex);
        NotificationRecipientInt recipient = getRecipientFromIndex(notification, recIndex);
        PhysicalAddressInt physicalAddressInt = recipient.getPhysicalAddress();
        String requestId = ChannelSenderUtils.buildPrepareAnalogDeliveryTimelineElementId(recIndex, notification.getIun(), FIRST_ATTEMPT);// Per l'invio di una notifica bonaria si presuppone che ci sia un solo invio
        PaperChannelPrepareRequest paperChannelPrepareRequest = PaperChannelPrepareRequest.builder()
                .requestId(requestId)
                .notificationInt(notification)
                .recipientInt(recipient)
                .paAddress(physicalAddressInt)
                .analogType(PhysicalAddressInt.ANALOG_TYPE.SIMPLE_REGISTERED_LETTER)
                .attachments(paperChannelUtils.retrieveAttachmentsToSend(notification, recIndex))
                .build();
        String msg = "Preparing simple registered letter notification with requestId {}";
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId, PnAuditLogEventType.AUD_COM_PD_PREPARE, msg);
        try {
            log.info("Preparing simple registered letter - iun={} recIndex={} requestId={}", notification.getIun(), recIndex, requestId);
            paperMessagesClient.prepare(paperChannelPrepareRequest);
            channelSenderUtils.savePrepareAnalogDeliveryElement(
                    recIndex,
                    notification,
                    requestId,
                    null,//Per l'invio di una notifica bonaria il dato serviceLevel è assente
                    FIRST_ATTEMPT,
                    null,//Per l'invio di una notifica bonaria si presuppone che ci sia un solo invio
                    physicalAddressInt);
            auditLogEvent.generateSuccess("simple registered letter notification prepare successfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error preparing simple registered letter notification", e).log();
            throw new PnInternalException(
                    "Error preparing simple registered letter notification for notification " + notification.getIun() + " to recipientIdx " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }

    @Override
    public String sendSimpleRegisteredLetter(NotificationInt notification,
                                             Integer recIndex,
                                             String prepareRequestId,
                                             PhysicalAddressInt receiverAddress,
                                             String productType,
                                             List<String> replacedF24AttachmentUrls,
                                             CategorizedAttachmentsResultInt categorizedAttachmentsResult
    ) {
        log.info("Start sendSimpleRegisteredLetter - iun={} recIndex={}", notification.getIun(), recIndex);
        String msg = "Sending simple registered letter notification with requestId {}";
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, prepareRequestId, PnAuditLogEventType.AUD_COM_PD_EXECUTE, msg);
        String timelineId;
        PaperChannelSendRequest paperChannelSendRequest = PaperChannelSendRequest.builder()
                .requestId(prepareRequestId)
                .notificationInt(notification)
                .recipientInt(getRecipientFromIndex(notification, recIndex))
                .productType(productType)
                .receiverAddress(receiverAddress)
                .attachments(getAttachments(categorizedAttachmentsResult))
                .arAddress(paperChannelUtils.getSenderAddress())
                .senderAddress(paperChannelUtils.getSenderAddress())
                .build();
        try {
            log.info("Sending simple registered letter - iun={} recIndex={} requestId={}", notification.getIun(), recIndex, prepareRequestId);
            SendResponse sendResponse = paperMessagesClient.send(paperChannelSendRequest);
            AnalogDtoInt analogDtoInfo = buildAnalogDto(prepareRequestId, productType, sendResponse);
            timelineId =
                    paperChannelUtils.addSendAnalogNotificationToTimeline(
                            notification,
                            receiverAddress,
                            recIndex,
                            analogDtoInfo,
                            replacedF24AttachmentUrls,
                            categorizedAttachmentsResult,
                            null,
                            AnalogDeliveryTypeInt.RS
                    );
            log.info("Registered Letter sent to paperChannel - iun={} id={}", notification.getIun(), recIndex);
            auditLogEvent.generateSuccess("send success cost={} send timelineId={}", sendResponse.getAmount(), timelineId).log();
            return timelineId;
        } catch (Exception exc) {
            auditLogEvent.generateFailure("failed send", exc).log();
            throw exc;
        }
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId, PnAuditLogEventType pnAuditLogEventType, String msg) {
        return auditLogService.buildAuditLogEvent(iun, recIndex, pnAuditLogEventType, msg, requestId);
    }
}
