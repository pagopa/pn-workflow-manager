package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
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

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep) {
        log.info("Sending email notification - iun={} recIndex={} currentStep={} channel={}",
                notification.getIun(), recIndex, currentStep, getChannelType());

        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        boolean emailMissing = ObjectUtils.isEmpty(recipient.getEmail());
        if (emailMissing) {
            handleMissingEmail(notification, campaign, recIndex, getChannelType(), recipient);
        } else {
            handleEmailPresent(notification, campaign, recIndex, currentStep, getChannelType(), recipient);
        }
    }

    private void handleMissingEmail(NotificationInt notification, Campaign campaign, int recIndex,
                                    ChannelType channel, NotificationRecipientInt recipient) {
        log.info("Recipient email is not present - iun={} recIndex={}", notification.getIun(), recIndex);
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
        log.info("Recipient email is present - iun={} recIndex={}", notification.getIun(), recIndex);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, channel);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);

        try {
            String subject = templateGeneratorService.generateEmailSubjectTemplate(notification, recipient);
            String htmlBody = templateGeneratorService.generateEmailBodyTemplate(notification, recipient, campaign);
            List<String> attachmentUrls = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, currentStep, campaign, channel);
            InformalDigitalAddressInt emailAddress = ChannelSenderUtils.buildDigitalAddress(
                    recipient.getEmail(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL
            );
            log.info("Sending email for notification {} to recipient {} with requestId {}",
                    notification.getIun(), recIndex, requestId);
            pnExternalChannelsClient.sendNotificationEMAIL(requestId, htmlBody, subject, notification, recipient, emailAddress, attachmentUrls);
            channelSenderUtils.saveSendDigitalMessageElement(
                    notification, requestId, recIndex, emailAddress,
                    DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL
            );
            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, channel);
            auditLogEvent.generateSuccess("Email sent successfully").log();
        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending message", e).log();
            throw new PnInternalException(
                    "Error sending email for notification " + notification.getIun() + " to recipient " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending email for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_EMAIL, msg, iun, recIndex, requestId);
    }

}
