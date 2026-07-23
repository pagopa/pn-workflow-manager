package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.PagoPaInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeClassification;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEventType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.utils.NotificationPaymentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;

@Component
@RequiredArgsConstructor
public class IoEventNormalizer implements ChannelOutcomeNormalizer<IoOutcomeEvent> {
    private final TimelineUtils timelineUtils;
    private final AuditLogService auditLogService;

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
                .pnAuditLogEvent(buildAuditLog(ioEvent, notification, recIndex))
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
            if(!StringUtils.hasText(ioEvent.getNoticeCode())) {
                String errorMessage = String.format(
                        "Received IO event of type PAID without noticeCode for notification %s and recipient index %d for requestId %s",
                        notification.getIun(),
                        recIndex,
                        ioEvent.getRequestId()
                );
                throw new PnInternalException(errorMessage, ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED);
            }
            PagoPaInt pagoPaInt = NotificationPaymentUtils.getPagoPaPaymentFromNoticeCode(notification, recIndex, ioEvent.getNoticeCode());
            triggers.add(
                    NotificationPaidInt.builder()
                            .iun(notification.getIun())
                            .noticeCode(ioEvent.getNoticeCode())
                            .creditorTaxId(pagoPaInt.getCreditorTaxId())
                            .eventTimestamp(ioEvent.getEventTimestamp())
                            .paymentSourceChannel(ChannelType.IO.name())
                            .amount(pagoPaInt.getAmount())
                            .build()
            );
        }
        return triggers;
    }

    private TimelineElementInternal buildTimelineElement(IoOutcomeEvent ioEvent, NotificationInt notificationInt, SendRelatedTimelineElement sourceSendRequestDetails, ChannelOutcomeClassification classification) {
        SendDigitalMessageDetailsInt digitalSendMessageDetails = (SendDigitalMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = digitalSendMessageDetails.getRecIndex();

        DigitalDeliveryDetailsInt digitalDeliveryDetails = DigitalDeliveryDetailsInt.builder()
                .code(ioEvent.getEventType().name())
                .eventTimestamp(ioEvent.getEventTimestamp())
                .build();

        return switch (classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignore -> timelineUtils.buildSendDigitalMessageProgress(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.IO,
                    ioEvent.getRequestId(),
                    digitalDeliveryDetails,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    ioEvent.getEventTimestamp()
            );
            case ChannelOutcomeCategory.Feedback ignore -> timelineUtils.buildSendDigitalMessageFeedback(
                    notificationInt,
                    recIndex,
                    DigitalChannelsInt.IO,
                    ioEvent.getRequestId(),
                    digitalDeliveryDetails,
                    digitalSendMessageDetails.getDigitalAddress(),
                    digitalSendMessageDetails.getDigitalAddressSource(),
                    ResponseStatusInt.KO,
                    null,
                    ioEvent.getEventTimestamp()
            );
        };
    }

    private PnAuditLogEvent buildAuditLog(IoOutcomeEvent ioEvent, NotificationInt notification, int recIndex) {
        String msg = String.format(
                "Received IO event: %s for notification %s and recipient index %d for requestId %s",
                ioEvent.getEventType().name(),
                notification.getIun(),
                recIndex,
                ioEvent.getRequestId()
        );
        return auditLogService.buildAuditLogEvent(notification.getIun(), recIndex, PnAuditLogEventType.AUD_COM_DD_RECEIVE,  msg);
    }
}