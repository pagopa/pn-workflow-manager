package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PecEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, int recIndex) {
        String eventCodeValue = pecEvent.getEventCode().getValue();

        // Get classification based on event code
        PecEventClassification classification = PecEventClassification.fromEventCode(eventCodeValue);

        // Build the timeline element based on the event type
        TimelineElementInternal timelineElement = buildTimelineElement(pecEvent, notification, recIndex);

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

    /**
     * Build the appropriate timeline element based on the event code.
     *
     * @param pecEvent the PEC outcome event
     * @param notification the notification details
     * @param recIndex the recipient index
     * @return the timeline element to persist
     */
    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent pecEvent, NotificationInt notification, int recIndex) {
        String eventCode = pecEvent.getEventCode().getValue();

        // Create delivery detail from event
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(eventCode)
                .eventTimestamp(pecEvent.getEventTimestamp())
                .build();

        // Check event type and build appropriate timeline element
        if (PecEventClassification.isProgressEvent(eventCode)) {
            // C000, C001, C007: Progress events
            return timelineUtils.buildSendDigitalMessageProgress(
                    notification,
                    recIndex,
                    DigitalChannelsInt.PEC,
                    pecEvent.getRequestId(),
                    deliveryDetail,
                    null,  // No digital address for PEC
                    null,  // No address source
                    pecEvent.getEventTimestamp()
            );
        } else {
            // C003, C002, C004, C006, C008, C009, C010, C011: Feedback events
            ResponseStatusInt responseStatus = PecEventClassification.isSuccessfulDelivery(eventCode)
                    ? ResponseStatusInt.OK
                    : ResponseStatusInt.KO;

            return timelineUtils.buildSendDigitalMessageFeedback(
                    notification,
                    recIndex,
                    DigitalChannelsInt.PEC,
                    pecEvent.getRequestId(),
                    deliveryDetail,
                    null,  // No digital address for PEC
                    null,  // No address source
                    responseStatus,
                    null,  // No sending receipts
                    pecEvent.getEventTimestamp()
            );
        }
    }
}

