package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyMessageUtils;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.CourtesyRetryableErrorClassifier;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ExternalChannelEventType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class InformalSmsCourtesySender implements CourtesyAddressSender {
    private final AuditLogService auditLogService;
    private final PnExternalChannelsClient pnExternalChannelsClient;
    private final TemplateGeneratorService templateGeneratorService;
    private final CourtesyRetryableErrorClassifier retryableErrorClassifier;
    private final CourtesyMessageUtils courtesyMessageUtils;

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.INFORMAL;
    }

    @Override
    public CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT getCourtesyAddressType() {
        return CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS;
    }

    @Override
    public CourtesySendOutcome send(NotificationInt notification, CourtesyDigitalAddressInt address, int recIndex) {
        String requestId = CourtesyMessageUtils.getSendCourtesyTimelineElementId(recIndex, notification.getIun(), getCourtesyAddressType(), Boolean.FALSE);
        PnAuditLogEvent auditLogEvent = buildAuditLogEvent(notification.getIun(), recIndex, requestId);
        try {
            String subject = templateGeneratorService.generateCourtesySmsTemplate(notification, notification.getRecipients().get(recIndex));
            pnExternalChannelsClient.sendNotificationSMS(requestId, subject, address.getAddress(), ExternalChannelEventType.COURTESY);
        } catch (Exception e) {
            boolean retryable = retryableErrorClassifier.isRetryableTransportError(address.getType(), e);
            auditLogEvent.generateFailure("Error sending courtesy message on channel={} retryable={} - iun={} id={}", address.getType(), retryable, notification.getIun(), recIndex, e);
            return retryable ? CourtesySendOutcome.RETRYABLE_ERROR : CourtesySendOutcome.PERMANENT_FAILURE;
        }

        courtesyMessageUtils.addSendCourtesyMessageToTimeline(notification, recIndex, address, Instant.now(), requestId, null);
        auditLogEvent.generateSuccess("Courtesy SMS sent successfully - iun={} id={}", notification.getIun(), recIndex);
        return CourtesySendOutcome.SENT;
    }

    private PnAuditLogEvent buildAuditLogEvent(String iun, int recIndex, String requestId) {
        String msg = "Sending courtesy email for notification {} to recipient {} with requestId {}";
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_SEND_SMS_COURTESY , msg, iun, recIndex, requestId);
    }
}
