package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.action.sendcourtesy.registry.CourtesyChannelSenderRegistry;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.SendCourtesyMessageActionDetails;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.CourtesyChannelFailureReasonInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendCourtesyMessageHandler {
    private final InformalCourtesyAddressResolver informalCourtesyAddressResolver;
    private final CourtesyMessageUtils courtesyMessageUtils;
    private final NotificationService notificationService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final SchedulerService schedulerService;
    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final CourtesyChannelSenderRegistry courtesyChannelSenderRegistry;

    /** Entry point for the {@code SEND_COURTESY_MESSAGE_ACTION}: send the courtesy on a single channel and handle the outcome. */
    public void handleSendCourtesyMessageAction(String iun, Integer recIndex, SendCourtesyMessageActionDetails details) {
        NotificationInt notification = notificationService.getInformalNotificationByIun(iun);
        CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel = details.getChannel();
        log.info("handleSendCourtesyMessageAction channel={} retryIndex={} deliveryMode={} - iun={} id={}", channel, details.getRetryIndex(), details.getDeliveryMode(), iun, recIndex);

        CourtesyDigitalAddressInt courtesyAddress = resolveCourtesyAddress(notification, recIndex, channel);
        if (courtesyAddress == null) {
            log.warn("Courtesy address not found for channel={}, closing channel - iun={} id={}", channel, iun, recIndex);
            closeCourtesyChannelWithoutSuccess(notification, recIndex, details, CourtesyChannelFailureReasonInt.EXPECTED_FAILURE);
            return;
        }

        CourtesyAddressSender courtesyAddressSender = courtesyChannelSenderRegistry.getSender(notification.getCommunicationType(), courtesyAddress.getType());
        CourtesySendOutcome outcome = courtesyAddressSender.send(notification, courtesyAddress, recIndex);

        switch (outcome) {
            case SENT -> log.info("Courtesy message sent successfully for channel={} retryIndex={} - iun={} id={}", channel, details.getRetryIndex(), iun, recIndex);
            case RETRYABLE_ERROR -> {
                log.info("Retryable error on courtesy channel={} retryIndex={} - iun={} id={}", channel, details.getRetryIndex(), iun, recIndex);
                handleRetryableError(notification, recIndex, details);
            }
            case PERMANENT_FAILURE -> {
                log.info("Courtesy message not sent for channel={}, permanent failure, channel closed - iun={} id={}", channel, iun, recIndex);
                closeCourtesyChannelWithoutSuccess(notification, recIndex, details, CourtesyChannelFailureReasonInt.EXPECTED_FAILURE);
            }
        }
    }

    /**
     * Reschedule the same action with the next backoff interval; the incremented retryIndex is carried in the details
     * so the resulting {@code actionId} stays unique and is not deduplicated by pn-action-manager.
     */
    private void handleRetryableError(NotificationInt notification, Integer recIndex, SendCourtesyMessageActionDetails details) {
        final String iun = notification.getIun();
        CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel = details.getChannel();
        List<Integer> intervals = resolveRetryIntervalsMinutes(channel);
        int currentRetryIndex = details.getRetryIndex();

        if (currentRetryIndex >= intervals.size()) {
            log.info("Courtesy retry intervals exhausted for channel={} retryIndex={}, channel closed - iun={} id={}",
                    channel, currentRetryIndex, iun, recIndex);
            closeCourtesyChannelWithoutSuccess(notification, recIndex, details, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
            return;
        }

        int waitMinutes = intervals.get(currentRetryIndex);
        int nextRetryIndex = currentRetryIndex + 1;
        Instant schedulingDate = Instant.now().plus(Duration.ofMinutes(waitMinutes));
        SendCourtesyMessageActionDetails nextDetails = SendCourtesyMessageActionDetails.builder()
                .channel(channel)
                .retryIndex(nextRetryIndex)
                .deliveryMode(details.getDeliveryMode())
                .plannedChannels(details.getPlannedChannels())
                .build();
        log.info("Rescheduling SEND_COURTESY_MESSAGE_ACTION channel={} nextRetryIndex={} waitMinutes={} schedulingDate={} - iun={} id={}",
                channel, nextRetryIndex, waitMinutes, schedulingDate, iun, recIndex);
        schedulerService.scheduleEvent(iun, recIndex, schedulingDate, ActionType.SEND_COURTESY_MESSAGE_ACTION, nextDetails);
    }

    /** Per-channel backoff intervals (minutes): size = number of retries, each value = wait before that retry; empty = no retry. */
    private List<Integer> resolveRetryIntervalsMinutes(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel) {
        PnWorkflowManagerConfigs.CourtesyRetry courtesyRetry = pnWorkflowManagerConfigs.getCourtesyRetry();
        if (courtesyRetry == null || courtesyRetry.getIntervalsMinutes() == null) {
            return List.of();
        }
        PnWorkflowManagerConfigs.CourtesyRetry.IntervalsMinutes intervalsMinutes = courtesyRetry.getIntervalsMinutes();
        List<Integer> channelIntervals = switch (channel) {
            case EMAIL -> intervalsMinutes.getEmail();
            case SMS -> intervalsMinutes.getSms();
            case APPIO -> intervalsMinutes.getIo();
            case TPP -> intervalsMinutes.getTpp();
        };
        return channelIntervals != null ? channelIntervals : List.of();
    }

    /** Record the channel failure on the timeline and, on the ANALOG branch, evaluate whether to start the analog workflow. */
    private void closeCourtesyChannelWithoutSuccess(NotificationInt notification, Integer recIndex, SendCourtesyMessageActionDetails details, CourtesyChannelFailureReasonInt failureReason) {
        addCourtesyChannelFailedToTimeline(notification, recIndex, details, failureReason);
    }

    private void addCourtesyChannelFailedToTimeline(NotificationInt notification, Integer recIndex, SendCourtesyMessageActionDetails details, CourtesyChannelFailureReasonInt failureReason) {
        String eventId = courtesyChannelFailedEventId(notification.getIun(), recIndex, details.getChannel());
        timelineService.addTimelineElement(
                timelineUtils.buildCourtesyChannelFailedTimelineElement(recIndex, notification, details.getChannel(), details.getDeliveryMode(), failureReason, eventId),
                notification
        );
    }


    private static String courtesyChannelFailedEventId(String iun, Integer recIndex, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel) {
        return TimelineEventId.COURTESY_CHANNEL_FAILED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .courtesyAddressType(channel)
                .build());
    }

    private CourtesyDigitalAddressInt resolveCourtesyAddress(NotificationInt notification, Integer recIndex, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel) {
        return informalCourtesyAddressResolver.resolveAddresses(notification.getRecipients().get(recIndex).getInternalId(), notification.getSender().getPaId()).stream()
                .filter(address -> channel.equals(address.getType()))
                .findFirst()
                .orElse(null);
    }

}
