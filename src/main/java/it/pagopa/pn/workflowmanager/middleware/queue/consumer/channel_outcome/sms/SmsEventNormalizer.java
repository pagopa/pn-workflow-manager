package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.DigitalMessageReferenceInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SmsEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;
    private final AuditLogService auditLogService;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent smsEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        String eventCode = smsEvent.getEventCode().getValue();
        SmsEventClassification classification = SmsEventClassification.fromEventCode(eventCode);
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = sourceSendRequestDetails.getRecIndex();

        TimelineElementInternal timelineElementInternal = buildTimelineElement(smsEvent, notification, digitalSendMessageDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.SMS)
                .timelineElementInternal(timelineElementInternal)
                .originalEventType(eventCode)
                .eventTimestamp(smsEvent.getEventTimestamp())
                .pnAuditLogEvent(buildAuditLog(smsEvent, notification, recIndex, digitalSendMessageDetails))
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent smsEvent, NotificationInt notification, SendDigitalMessageDetailsInt digitalSendMessageDetails, SmsEventClassification classification) {
        int recIndex = digitalSendMessageDetails.getRecIndex();

        String eventCode = smsEvent.getEventCode().getValue();
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(eventCode)
                .eventTimestamp(smsEvent.getEventTimestamp())
                .build();

        return timelineUtils.buildSendDigitalMessageFeedback(
                notification,
                recIndex,
                DigitalChannelsInt.SMS,
                smsEvent.getRequestId(),
                deliveryDetail,
                digitalSendMessageDetails.getDigitalAddress(),
                digitalSendMessageDetails.getDigitalAddressSource(),
                classification.getCategory().isNegativeFeedback() ? ResponseStatusInt.KO : ResponseStatusInt.OK,
                null,
                smsEvent.getEventTimestamp()
        );
    }

    private PnAuditLogEvent buildAuditLog(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, int recIndex, SendDigitalMessageDetailsInt digitalSendMessageDetails) {
        DigitalMessageReferenceInt digitalMessageReference = emailEvent.getGeneratedMessage();
        String attachments = (digitalMessageReference!=null && digitalMessageReference.getLocation()!=null)?digitalMessageReference.getLocation():"";

        String msg = String.format(
                "Received sent SMS outcome event: %s for notification %s and recipient index %d for source %s, status=%s, attachments=%s",
                emailEvent.getEventCode().getValue(),
                notification.getIun(),
                recIndex,
                digitalSendMessageDetails.getDigitalAddressSource(),
                emailEvent.getStatus(),
                attachments
        );
        return auditLogService.buildAuditLogEvent(notification.getIun(), recIndex, PnAuditLogEventType.AUD_COM_DD_RECEIVE, msg);
    }
}

