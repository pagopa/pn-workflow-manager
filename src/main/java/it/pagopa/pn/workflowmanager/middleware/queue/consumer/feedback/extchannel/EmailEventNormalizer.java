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
public class EmailEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, int recIndex) {
        String eventCode = emailEvent.getEventCode().getValue();
        EmailEventClassification classification = EmailEventClassification.fromEventCode(eventCode);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.EMAIL)
                .timelineElementInternal(buildTimelineElement(emailEvent, notification, recIndex))
                .originalEventType(eventCode)
                .eventTimestamp(emailEvent.getEventTimestamp())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(ExtChannelOutcomeEvent emailEvent, NotificationInt notification, int recIndex) {
        String eventCode = emailEvent.getEventCode().getValue();
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(eventCode)
                .eventTimestamp(emailEvent.getEventTimestamp())
                .build();

        if (EmailEventClassification.isProgressEvent(eventCode)) {
            return timelineUtils.buildSendDigitalMessageProgress(
                    notification,
                    recIndex,
                    DigitalChannelsInt.EMAIL,
                    emailEvent.getRequestId(),
                    deliveryDetail,
                    null,
                    null,
                    emailEvent.getEventTimestamp()
            );
        }

        return timelineUtils.buildSendDigitalMessageFeedback(
                notification,
                recIndex,
                DigitalChannelsInt.EMAIL,
                emailEvent.getRequestId(),
                deliveryDetail,
                null,
                null,
                ResponseStatusInt.KO,
                null,
                emailEvent.getEventTimestamp()
        );
    }
}

