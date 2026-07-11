package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector.IoConnectorClient;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;


@Component
@Slf4j
@RequiredArgsConstructor
public class IoChannelSender implements ChannelSender {
    private final AuditLogService auditLogService;
    private final TemplateGeneratorService templateGeneratorService;
    private final IoConnectorClient ioConnectorClient;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.IO;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep) {
        log.info("Sending message for notification {} to recipient {}", notification.getIun(), recIndex);
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, getChannelType());
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);
        try {
            String markdown = templateGeneratorService.generateIoMessageTemplate(notification, recipient, campaign);

            ioConnectorClient.sendMessage(
                    IoMessageRequest.builder()
                            .requestId(requestId)
                            .markdown(markdown)
                            .notificationInt(notification)
                            .notificationRecipientInt(recipient)
                            .campaign(campaign)
                            .build()
            );

            channelSenderUtils.saveSendDigitalMessageElement(
                    notification,
                    requestId,
                    recIndex,
                    ChannelSenderUtils.buildDigitalAddress(recipient.getTaxId(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.APPIO),
                    DigitalChannelsInt.IO,
                    null
            );

            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, getChannelType());
            auditLogEvent.generateSuccess("Message sent succesfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending message", e).log();
            throw new PnInternalException("Error sending message for notification " + notification.getIun() + " to recipient " + recIndex, ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }

    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending message for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_IO, msg, iun, recIndex, requestId);
    }
}
