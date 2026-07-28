package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.pagopa.pn.workflowmanager.action.utils.PnConstants.FIRST_ATTEMPT;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Component
@RequiredArgsConstructor
@CustomLog
public class PecChannelSender implements ChannelSender {

    private final TemplateGeneratorService templateGeneratorService;
    private final PnExternalChannelsClient pnExternalChannelsClient;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;
    private final AuditLogService auditLogService;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PEC;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep) {
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);

        String timelineId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, getChannelType(),FIRST_ATTEMPT);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, timelineId);

        try {
            String messageText = templateGeneratorService.generatePecBodyTemplate(notification, recipient, campaign);
            String subject = templateGeneratorService.generatePecSubjectTemplate(notification, recipient);

            List<String> attachmentUrls = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, getChannelType());

            pnExternalChannelsClient.sendNotificationPEC(
                    timelineId,
                    messageText,
                    subject,
                    notification,
                    recipient,
                    recipient.getDigitalDomicile(),
                    attachmentUrls
            );

            channelSenderUtils.saveSendDigitalMessageElement(
                    notification,
                    timelineId,
                    recIndex,
                    ChannelSenderUtils.buildDigitalAddress(recipient.getDigitalDomicile().getAddress(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC),
                    DigitalChannelsInt.PEC,
                    null
            );

            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, getChannelType());
            auditLogEvent.generateSuccess("Pec sent successfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending pec", e).log();
            throw new PnInternalException(
                    "Error sending pec for notification " + notification.getIun() + " to recipient " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }

    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending pec for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_PEC, msg, iun, recIndex, requestId);
    }
}

