package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.io;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.utils.NotificationPaymentUtils;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class IoEventNormalizer implements ChannelOutcomeNormalizer<IoOutcomeEvent> {
    private final TimelineUtils timelineUtils;

    @Override
    public NormalizedChannelOutcome normalize(IoOutcomeEvent ioEvent, NotificationInt notification, int recIndex) {
        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .classification(IoEventClassification.fromEventType(ioEvent.getEventType().name()))
                .triggers(buildTriggers(ioEvent, notification, recIndex))
                .channel(ChannelType.IO)
                .timelineElementInternal(buildTimelineElement(ioEvent, notification, recIndex))
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

    private TimelineElementInternal buildTimelineElement(IoOutcomeEvent ioEvent, NotificationInt notificationInt, int recIndex) {
        return switch(ioEvent.getEventType()) {
            case SENT_TO_IO, READ, PAID, DELIVERED_TO_USER -> timelineUtils.buildSendDigitalMessageProgress(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.APPIO,
                    ioEvent.getRequestId(),
                    DigitalDeliveryDetailsInt.builder()
                            .code(ioEvent.getEventType().name())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .build(),
                    null, // TODO: Mettiamo il taxCode del destinatario come address?
                    null,
                    ioEvent.getEventTimestamp()
            );
            case SENDER_NOT_ALLOWED -> timelineUtils.buildSendDigitalMessageFeedback(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.APPIO,
                    ioEvent.getRequestId(),
                    DigitalDeliveryDetailsInt.builder()
                            .code(ioEvent.getEventType().name())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .build(),
                    null, // TODO: Mettiamo il taxCode del destinatario come address?
                    null,
                    ResponseStatusInt.KO,
                    null,
                    ioEvent.getEventTimestamp()
            );
        };
    }
}