package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
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
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PecEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        String eventCodeValue = pecEvent.getEventCode().getValue();
        int recIndex = sourceSendRequestDetails.getRecIndex();

        PecEventClassification classification = PecEventClassification.fromEventCode(eventCodeValue);

        TimelineElementInternal timelineElement = buildTimelineElement(pecEvent, notification, sourceSendRequestDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.PEC)
                .timelineElementInternal(timelineElement)
                .originalEventType(eventCodeValue)
                .eventTimestamp(pecEvent.getEventTimestamp())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails, PecEventClassification classification) {
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
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
}

