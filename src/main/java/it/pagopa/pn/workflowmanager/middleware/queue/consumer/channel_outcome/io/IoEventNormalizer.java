package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.utils.NotificationPaymentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IoEventNormalizer implements ChannelOutcomeNormalizer<IoOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(IoOutcomeEvent ioEvent, NotificationInt notification, SendRelatedTimelineElement sourceSendRequestDetails) {
        int recIndex = sourceSendRequestDetails.getRecIndex();
        ChannelOutcomeClassification classification = IoEventClassification.fromEventType(ioEvent.getEventType().name());
        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .classification(classification)
                .triggers(buildTriggers(ioEvent, notification, recIndex))
                .channel(ChannelType.IO)
                .timelineElementInternal(buildTimelineElement(ioEvent, notification, sourceSendRequestDetails, classification))
                .originalEventType(ioEvent.getEventType().name())
                .eventTimestamp(ioEvent.getEventTimestamp())
                .build();
    }

    private Set<ChannelEventTrigger> buildTriggers(IoOutcomeEvent ioEvent, NotificationInt notification, int recIndex) {
        Set<ChannelEventTrigger> triggers = new HashSet<>();
        if(ioEvent.getEventType() == IoOutcomeEventType.READ) {
            triggers.add(
                    NotificationViewedInt.builder()
                        .iun(notification.getIun())
                        .recipientIndex(recIndex)
                        .sourceChannel(ChannelType.IO.name())
                        .viewedDate(ioEvent.getEventTimestamp())
                        .build()
            );
        } else if(ioEvent.getEventType() == IoOutcomeEventType.PAID) {
            triggers.add(
                    NotificationPaidInt.builder()
                            .iun(notification.getIun())
                            .noticeCode(ioEvent.getNoticeCode())
                            .creditorTaxId(notification.getSender().getPaTaxId())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .paymentSourceChannel(ChannelType.IO.name())
                            .amount(NotificationPaymentUtils.getAmountFromNotificationPagoPaPayment(notification, recIndex, ioEvent.getNoticeCode()))
                            .build()
            );
        }
        return triggers;
    }

    private TimelineElementInternal buildTimelineElement(IoOutcomeEvent ioEvent, NotificationInt notificationInt, SendRelatedTimelineElement sourceSendRequestDetails, ChannelOutcomeClassification classification) {
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = digitalSendMessageDetails.getRecIndex();
        return switch (classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignore -> timelineUtils.buildSendDigitalMessageProgress(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.IO,
                    ioEvent.getRequestId(),
                    DigitalDeliveryDetailsInt.builder()
                            .code(ioEvent.getEventType().name())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .build(),
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    ioEvent.getEventTimestamp()
            );
            case ChannelOutcomeCategory.Feedback ignore -> timelineUtils.buildSendDigitalMessageFeedback(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.IO,
                    ioEvent.getRequestId(),
                    DigitalDeliveryDetailsInt.builder()
                            .code(ioEvent.getEventType().name())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .build(),
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    ResponseStatusInt.KO,
                    null,
                    ioEvent.getEventTimestamp()
            );
        };
    }
}