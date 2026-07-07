package it.pagopa.pn.workflowmanager.action.start_workflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.utils.AttachmentUtils;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private final AuditLogService auditLogService;
    private final PnExternalChannelsClient pnExternalChannelsClient;
    private final TemplateGeneratorService templateGeneratorService;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;
    private final AttachmentUtils attachmentUtils;

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep, ChannelType channel) {
        log.info("Sending email notification - iun={} recIndex={} currentStep={} channel={}",
                notification.getIun(), recIndex, currentStep, channel);

        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        boolean emailMissing = ObjectUtils.isEmpty(recipient.getEmail());
        if (emailMissing) {
            handleMissingEmail(notification, campaign, recIndex, currentStep, channel, recipient);
        } else {
            handleEmailPresent(notification, campaign, recIndex, currentStep, channel, recipient);
        }
    }

    private void handleMissingEmail(NotificationInt notification, Campaign campaign, int recIndex,
                                    int currentStep, ChannelType channel,
                                    NotificationRecipientInt recipient) {
        log.info("Recipient email is not present - iun={} recIndex={} currentStep={} channel={}",
                notification.getIun(), recIndex, currentStep, channel);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(recIndex, notification.getIun(), channel);
        channelSenderUtils.saveSendDigitalMessageSkipElement(
                recIndex, notification, requestId,
                DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL
        );
        workflowUtils.advanceWorkflow(
                notification.getIun(), recIndex, channel, campaign, recipient.getRecipientType()
        );
    }

    private void handleEmailPresent(NotificationInt notification, Campaign campaign, int recIndex,
                                    int currentStep, ChannelType channel,
                                    NotificationRecipientInt recipient) {
        log.info("Recipient email is present - iun={} recIndex={} currentStep={} channel={}",
                notification.getIun(), recIndex, currentStep, channel);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, channel);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);

        try {
            boolean userFromAppIo = channelSenderUtils.searchIfUserFromAppIo(notification.getIun(), channel, recIndex);
            String html = templateGeneratorService.generateInformalIoCommunicationTemplate(notification, recipient, userFromAppIo);
            List<String> attachmentUrls = resolveAttachments(notification, recIndex, currentStep, campaign, channel);
            InformalDigitalAddressInt emailAddress = ChannelSenderUtils.buildDigitalAddress(
                    recipient.getEmail(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL
            );
            log.info("Sending email for notification {} to recipient {} with requestId {}",
                    notification.getIun(), recIndex, requestId);
            pnExternalChannelsClient.sendNotificationEMAIL(requestId, html, notification, recipient, emailAddress, attachmentUrls);
            channelSenderUtils.saveSendDigitalMessageElement(
                    notification, requestId, recIndex, emailAddress,
                    DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL
            );
            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, currentStep, campaign, channel);
            auditLogEvent.generateSuccess("Email sent successfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending message", e).log();
            throw new PnInternalException(
                    "Error sending email for notification " + notification.getIun() + " to recipient " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }


    private List<String> resolveAttachments(NotificationInt notification, int recIndex,
                                            int currentStep, Campaign campaign, ChannelType channel) {
        boolean includeAttachment = Boolean.TRUE.equals(campaign.getWorkflow().get(currentStep).getIncludeAttachment());
        if (!includeAttachment) {
            return List.of();
        }
        return attachmentUtils.retrieveAttachments(
                notification, recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, channel),
                false
        );
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending email for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_EMAIL, msg, iun, recIndex, requestId);
    }

}
