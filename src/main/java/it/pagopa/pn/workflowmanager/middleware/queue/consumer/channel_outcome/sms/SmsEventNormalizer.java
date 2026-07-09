package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

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
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SmsEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent smsEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        String eventCode = smsEvent.getEventCode().getValue();
        SmsEventClassification classification = SmsEventClassification.fromEventCode(eventCode);
        int recIndex = sourceSendRequestDetails.getRecIndex();

        TimelineElementInternal timelineElementInternal = buildTimelineElement(smsEvent, notification, sourceSendRequestDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.SMS)
                .timelineElementInternal(timelineElementInternal)
                .originalEventType(eventCode)
                .eventTimestamp(smsEvent.getEventTimestamp())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent smsEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails, SmsEventClassification classification) {
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
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
                classification.isSuccessEvent() ? ResponseStatusInt.OK : ResponseStatusInt.KO,
                null,
                smsEvent.getEventTimestamp()
        );
    }
}

