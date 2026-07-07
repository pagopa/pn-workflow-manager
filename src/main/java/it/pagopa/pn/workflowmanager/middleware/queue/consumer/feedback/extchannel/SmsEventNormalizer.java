package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
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
public class SmsEventNormalizer implements ChannelOutcomeNormalizer<ExtChannelOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(ExtChannelOutcomeEvent smsEvent, NotificationInt notification, int recIndex) {
        String eventCode = smsEvent.getEventCode().getValue();
        SmsEventClassification classification = SmsEventClassification.fromEventCode(eventCode);

        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code(eventCode)
                .eventTimestamp(smsEvent.getEventTimestamp())
                .build();

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.SMS)
                .timelineElementInternal(
                        timelineUtils.buildSendDigitalMessageFeedback(
                                notification,
                                recIndex,
                                DigitalChannelsInt.SMS,
                                smsEvent.getRequestId(),
                                deliveryDetail,
                                null,
                                null,
                                SmsEventClassification.isSuccessEvent(eventCode) ? ResponseStatusInt.OK : ResponseStatusInt.KO,
                                null,
                                smsEvent.getEventTimestamp()
                        )
                )
                .originalEventType(eventCode)
                .eventTimestamp(smsEvent.getEventTimestamp())
                .build();
    }
}

