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
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ExternalChannelEventType;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import it.pagopa.pn.workflowmanager.utils.AddressSearchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.action.utils.PnConstants.FIRST_ATTEMPT;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsChannelSender implements ChannelSender {

    private final AuditLogService auditLogService;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;
    private final TemplateGeneratorService templateGeneratorService;
    private final AddressSearchUtils addressSearchUtils;
    private final PnExternalChannelsClient pnExternalChannelsClient;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, DigitalAddressSourceInt addressSource) {
        log.info("Sending sms notification - iun={} recIndex={} addressSource={} channel={}",
                notification.getIun(), recIndex, addressSource, getChannelType());
        if (addressSource == DigitalAddressSourceInt.NONE) {
            handleMissingPhoneNumber(notification, campaign, recIndex);
        } else {
            handlePhoneNumberPresent(notification, campaign, recIndex, addressSource);
        }
    }

    private void handleMissingPhoneNumber(NotificationInt notification, Campaign campaign, int recIndex) {
        log.info("Recipient phone number is not present - iun={} recIndex={}", notification.getIun(), recIndex);

        String requestId = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(recIndex, notification.getIun(), getChannelType());
        channelSenderUtils.saveSendDigitalMessageSkipElement(
                recIndex, notification, requestId, DigitalChannelsInt.SMS
        );
        workflowUtils.advanceWorkflow(notification.getIun(), recIndex, getChannelType(), campaign, notification.getRecipients().get(recIndex).getRecipientType());
    }

    private void handlePhoneNumberPresent(NotificationInt notification, Campaign campaign, int recIndex, DigitalAddressSourceInt addressSource) {
        log.info("Recipient phone number is present - iun={} recIndex={}", notification.getIun(), recIndex);

        String requestId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, getChannelType(), FIRST_ATTEMPT);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);

        try {
            InformalDigitalAddressInt smsAddress = addressSearchUtils.retrieveDigitalAddressFromTimeline(notification, recIndex,
                    addressSource, DigitalChannelsInt.SMS, FIRST_ATTEMPT);

            String subject = templateGeneratorService.generateSmsTemplate(notification, notification.getRecipients().get(recIndex));

            log.info("Sending SMS for notification {} to recipient {} with requestId {}", notification.getIun(), recIndex, requestId);
            pnExternalChannelsClient.sendNotificationSMS(requestId, subject, smsAddress.getAddress(), ExternalChannelEventType.INFORMAL);

            channelSenderUtils.saveSendDigitalMessageElement(notification, requestId, recIndex, smsAddress, DigitalChannelsInt.SMS,
                    addressSource);
            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, getChannelType());
            auditLogEvent.generateSuccess("Sms sent successfully").log();

        } catch (Exception e) {
            auditLogEvent.generateFailure("Error sending SMS notification", e).log();
            throw new PnInternalException(
                    "Error sending SMS for notification " + notification.getIun() + " to recipient " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_SMS,
                "Sending message for notification {} to recipient {} with requestId {}", iun, recIndex, requestId);
    }
}
