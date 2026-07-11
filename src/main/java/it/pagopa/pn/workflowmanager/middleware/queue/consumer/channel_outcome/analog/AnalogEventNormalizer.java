package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.SendEventInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnalogEventNormalizer implements ChannelOutcomeNormalizer<SendEventInt> {

    private static final Integer FIRST_ATTEMPT = 0;
    private final TimelineUtils timelineUtils;


    @Override
    public NormalizedChannelOutcome normalize(SendEventInt sendEvent,
                                              NotificationInt notification,
                                              SendRelatedTimelineElement sourceSendRequestDetails
    ) {

        int recIndex = sourceSendRequestDetails.getRecIndex();

        String statusEventCode = sendEvent.getStatusDescription();

        AnalogEventClassification classification = AnalogEventClassification.fromStatusEventCode(statusEventCode);

        TimelineElementInternal timelineElement = buildTimelineElement(sendEvent, notification, sourceSendRequestDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.ANALOG)
                .timelineElementInternal(timelineElement)
                .originalEventType(statusEventCode)
                .eventTimestamp(sendEvent.getStatusDateTime())
                .build();
    }

    private TimelineElementInternal buildTimelineElement(SendEventInt sendEventInt,
                                                         NotificationInt notification,
                                                         SendRelatedTimelineElement sourceSendRequestDetails,
                                                         AnalogEventClassification classification
    ) {
        SendAnalogMessageDetailsInt analogSendMessageDetails = (SendAnalogMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = analogSendMessageDetails.getRecIndex();

        // Create analog detail from event
        AnalogDeliveryDetailsInt analogDeliveryDetails = AnalogDeliveryDetailsInt.builder()
                .code(sendEventInt.getStatusCode())
                .eventTimestamp(sendEventInt.getStatusDateTime())
                .build();

        return switch(classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignore -> timelineUtils.buildSendAnalogProgressNotificationTimelineElement(
                    recIndex,
                    notification,
                    null,
                    notification.getSentAt(),
                    analogDeliveryDetails,
                    AnalogDeliveryTypeInt.RS,
                    sendEventInt.getAttachments(),
                    sendEventInt.getRequestId(),
                    sendEventInt.getRegisteredLetterCode(),
                    FIRST_ATTEMPT
            );
            case ChannelOutcomeCategory.Feedback feedback -> timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                    recIndex,
                    notification,
                    null,
                    notification.getSentAt(),
                    analogDeliveryDetails,
                    AnalogDeliveryTypeInt.RS,
                    sendEventInt.getAttachments(),
                    sendEventInt.getRequestId(),
                    sendEventInt.getRegisteredLetterCode(),
                    sendEventInt.getDiscoveredAddress(),
                    determineStatus(feedback),
                    null,//ToDo: Per ora campo inutilizzato
                    null,
                    FIRST_ATTEMPT
            );
        };
    }

    private ResponseStatusInt determineStatus(ChannelOutcomeCategory.Feedback feedback) {
        return feedback.isNegativeFeedback() ? ResponseStatusInt.KO : ResponseStatusInt.OK;
    }
}
