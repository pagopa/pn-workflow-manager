package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.DigitalMessageReferenceInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PecEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;
    private final AuditLogService auditLogService;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        String eventCodeValue = pecEvent.getEventCode().getValue();
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = digitalSendMessageDetails.getRecIndex();

        PecEventClassification classification = PecEventClassification.fromEventCode(eventCodeValue);

        TimelineElementInternal timelineElement = buildTimelineElement(pecEvent, notification, digitalSendMessageDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.PEC)
                .timelineElementInternal(timelineElement)
                .originalEventType(eventCodeValue)
                .eventTimestamp(pecEvent.getEventTimestamp())
                .pnAuditLogEvent(buildAuditLog(pecEvent, notification, recIndex, digitalSendMessageDetails))
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, SendDigitalMessageDetailsInt digitalSendMessageDetails, PecEventClassification classification) {
        int recIndex = digitalSendMessageDetails.getRecIndex();

        // Create delivery detail from event
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(pecEvent.getEventCode().getValue())
                .eventTimestamp(pecEvent.getEventTimestamp())
                .build();

        return switch(classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignore -> timelineUtils.buildSendDigitalMessageProgress(
                    notification,
                    recIndex,
                    DigitalChannelsInt.PEC,
                    pecEvent.getRequestId(),
                    deliveryDetail,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    pecEvent.getEventTimestamp()
            );
            case ChannelOutcomeCategory.Feedback f -> timelineUtils.buildSendDigitalMessageFeedback(
                    notification,
                    recIndex,
                    DigitalChannelsInt.PEC,
                    pecEvent.getRequestId(),
                    deliveryDetail,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    f.isNegativeFeedback() ? ResponseStatusInt.KO : ResponseStatusInt.OK,
                    mapSendingReceipts(pecEvent),
                    pecEvent.getEventTimestamp()
            );
        };
    }

    private List<SendingReceipt> mapSendingReceipts(ExtChannelOutcomeEvent pecEvent) {
        if(pecEvent.getGeneratedMessage() == null || pecEvent.getGeneratedMessage().getId() == null) {
            return null;
        }

        SendingReceipt sendingReceipt = new SendingReceipt();
        sendingReceipt.setId(pecEvent.getGeneratedMessage().getId());
        sendingReceipt.setSystem(pecEvent.getGeneratedMessage().getSystem());
        return List.of(sendingReceipt);
    }

    private PnAuditLogEvent buildAuditLog(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, int recIndex, SendDigitalMessageDetailsInt digitalSendMessageDetails) {
        DigitalMessageReferenceInt digitalMessageReference = emailEvent.getGeneratedMessage();
        String attachments = (digitalMessageReference!=null && digitalMessageReference.getLocation()!=null)?digitalMessageReference.getLocation():"";

        String msg = String.format(
                "Received sent PEC outcome event: %s for notification %s and recipient index %d for source %s, status=%s, attachments=%s",
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

