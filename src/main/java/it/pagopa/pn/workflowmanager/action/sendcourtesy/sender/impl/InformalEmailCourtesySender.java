package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.NotificationUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ExternalChannelEventType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InformalEmailCourtesySender implements CourtesyAddressSender {
    private final PnWorkflowManagerConfigs configs;
    private final AuditLogService auditLogService;
    private final PnExternalChannelsClient pnExternalChannelsClient;
    private final TemplateGeneratorService templateGeneratorService;
    private final CampaignService campaignService;
    private final ChannelSenderUtils channelSenderUtils;
    private final CourtesyRetryableErrorClassifier retryableErrorClassifier;
    private final CourtesyMessageUtils courtesyMessageUtils;

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.INFORMAL;
    }

    @Override
    public CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT getCourtesyAddressType() {
        return CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL;
    }

    @Override
    public CourtesySendOutcome send(NotificationInt notification, CourtesyDigitalAddressInt address, int recIndex) {
        String requestId = CourtesyMessageUtils.getSendCourtesyTimelineElementId(recIndex, notification.getIun(), getCourtesyAddressType(), Boolean.FALSE);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);
        try {
            Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(notification.getCampaignId(), notification.getSender().getPaId());
            NotificationRecipientInt recipient = NotificationUtils.getRecipientFromIndex(notification, recIndex);
            String subject = templateGeneratorService.generateCourtesyEmailSubjectTemplate(notification, recipient);
            String htmlBody = templateGeneratorService.generateCourtesyEmailBodyTemplate(notification, recipient, campaign);
            List<String> attachmentUrls = retrieveAttachmentUrls(notification, recIndex, campaign);
            InformalDigitalAddressInt emailAddress = toInformalDigitalAddress(address);

            pnExternalChannelsClient.sendNotificationEMAIL(requestId, htmlBody, subject, notification, recipient, emailAddress, attachmentUrls, ExternalChannelEventType.COURTESY);
        } catch (Exception e) {
            boolean retryable = retryableErrorClassifier.isRetryableTransportError(address.getType(), e);
            auditLogEvent.generateFailure("Error sending courtesy message on channel={} retryable={} - iun={} id={}", address.getType(), retryable, notification.getIun(), recIndex, e).log();
            return retryable ? CourtesySendOutcome.RETRYABLE_ERROR : CourtesySendOutcome.PERMANENT_FAILURE;
        }

        courtesyMessageUtils.addSendCourtesyMessageToTimeline(notification, recIndex, address, Instant.now(), requestId, null);
        auditLogEvent.generateSuccess("Courtesy email sent successfully - iun={} id={}", notification.getIun(), recIndex).log();
        return CourtesySendOutcome.SENT;
    }

    private InformalDigitalAddressInt toInformalDigitalAddress(CourtesyDigitalAddressInt address) {
        return InformalDigitalAddressInt.builder()
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL)
                .address(address.getAddress())
                .build();
    }

    private List<String> retrieveAttachmentUrls(NotificationInt notification, int recIndex, Campaign campaign) {
        if(Boolean.TRUE.equals(configs.getEmailCourtesyRequiresAttachments())) {
            return channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, ChannelType.EMAIL);
        }

        log.debug("Email courtesy message does not require attachments - iun={} id={}", notification.getIun(), recIndex);
        return List.of();
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending courtesy email for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_EMAIL_COURTESY, msg, iun, recIndex, requestId);
    }
}
