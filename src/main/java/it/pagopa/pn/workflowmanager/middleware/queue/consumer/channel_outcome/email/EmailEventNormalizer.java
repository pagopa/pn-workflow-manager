package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email;

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

@RequiredArgsConstructor
@Component
public class EmailEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        String eventCode = emailEvent.getEventCode().getValue();
        EmailEventClassification classification = EmailEventClassification.fromEventCode(eventCode);
        int recIndex = sourceSendRequestDetails.getRecIndex();

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.EMAIL)
                .timelineElementInternal(buildTimelineElement(emailEvent, notification, sourceSendRequestDetails, classification))
                .originalEventType(eventCode)
                .eventTimestamp(emailEvent.getEventTimestamp())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails, EmailEventClassification classification) {
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = digitalSendMessageDetails.getRecIndex();

        String eventCode = emailEvent.getEventCode().getValue();
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(eventCode)
                .eventTimestamp(emailEvent.getEventTimestamp())
                .build();

        return switch(classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignored -> timelineUtils.buildSendDigitalMessageProgress(
                    notification,
                    recIndex,
                    DigitalChannelsInt.EMAIL,
                    emailEvent.getRequestId(),
                    deliveryDetail,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    emailEvent.getEventTimestamp()
            );
            case ChannelOutcomeCategory.Feedback ignored -> timelineUtils.buildSendDigitalMessageFeedback(
                    notification,
                    recIndex,
                    DigitalChannelsInt.EMAIL,
                    emailEvent.getRequestId(),
                    deliveryDetail,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    ResponseStatusInt.KO, // Al momento sono attesi solo feedback negativi
                    null,
                    emailEvent.getEventTimestamp()
            );
        };
    }
}

