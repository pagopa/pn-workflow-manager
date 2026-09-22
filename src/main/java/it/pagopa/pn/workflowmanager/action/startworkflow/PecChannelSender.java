package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyAddressActionDispatcher;
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
    private final AddressSearchUtils addressSearchUtils;
    private final AuditLogService auditLogService;
    private final CourtesyAddressActionDispatcher courtesyAddressActionDispatcher;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PEC;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, DigitalAddressSourceInt addressSource) {
        log.info("Sending pec notification - iun={} recIndex={} addressSource={} channel={}",
                notification.getIun(), recIndex, addressSource, getChannelType());
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        boolean pecMissing = addressSource == DigitalAddressSourceInt.NONE;
        if (pecMissing) {
            handleMissingPec(notification, campaign, recIndex, getChannelType(), recipient);
        } else {
            handlePecPresent(notification, campaign, recIndex, getChannelType(), recipient, addressSource);
        }
    }

    private void handleMissingPec(NotificationInt notification, Campaign campaign, int recIndex,
                                    ChannelType channel, NotificationRecipientInt recipient) {
        log.info("Recipient pec is not present - iun={} recIndex={}", notification.getIun(), recIndex);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(recIndex, notification.getIun(), channel);
        channelSenderUtils.saveSendDigitalMessageSkipElement(
                recIndex, notification, requestId,
                DigitalChannelsInt.PEC
        );
        workflowUtils.advanceWorkflow(
                notification.getIun(), recIndex, channel, campaign, recipient.getRecipientType()
        );
    }

    private void handlePecPresent(NotificationInt notification, Campaign campaign, int recIndex, ChannelType channel, NotificationRecipientInt recipient, DigitalAddressSourceInt addressSource) {
        log.info("Sending pec for notification {} to recipient {} addressSource={}", notification.getIun(), recIndex, addressSource);

        String timelineId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, getChannelType(), FIRST_ATTEMPT);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, timelineId);

        try {
            InformalDigitalAddressInt digitalAddress = addressSearchUtils.retrieveDigitalAddressFromTimeline(notification, recIndex,
                    addressSource, DigitalChannelsInt.PEC, FIRST_ATTEMPT);

            String messageText = templateGeneratorService.generatePecBodyTemplate(notification, recipient, campaign);
            String subject = templateGeneratorService.generatePecSubjectTemplate(notification, recipient);

            List<String> attachmentUrls = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, getChannelType());

            pnExternalChannelsClient.sendNotificationPEC(
                    timelineId,
                    messageText,
                    subject,
                    notification,
                    recipient,
                    digitalAddress,
                    attachmentUrls
            );

            channelSenderUtils.saveSendDigitalMessageElement(
                    notification,
                    timelineId,
                    recIndex,
                    digitalAddress,
                    DigitalChannelsInt.PEC,
                    addressSource
            );

            workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, getChannelType());

            if (InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.SERCQ.equals(digitalAddress.getType())) {
                log.debug("scheduling courtesy messages for iun : {} recIndex : {}", notification.getIun(), recIndex);
                courtesyAddressActionDispatcher.dispatch(notification, recIndex);
            }
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

