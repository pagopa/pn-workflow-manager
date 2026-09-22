package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.GetAddressInfoDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InformalDigitalAddressRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@AllArgsConstructor
@Getter
@Component
@Slf4j
public class AddressSearchUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final SchedulerService schedulerService;
    private final ChannelSenderUtils channelSenderUtils;

    public Optional<GetAddressInfoDetailsInt> findPreviousSearchOutcome(
            String iun,
            int recIndex,
            DigitalAddressSourceInt source,
            ChannelType channelType,
            int sendAttempt
    ) {
        String timelineElementId = TimelineEventId.GET_ADDRESS.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .source(source)
                        .sentAttemptMade(sendAttempt)
                        .channel(channelType.name())
                        .build()
        );

        Optional<TimelineElementInternal> timelineElement = timelineService.getTimelineElement(iun, timelineElementId);

        return timelineElement.map(timelineElementInternal -> (GetAddressInfoDetailsInt) timelineElementInternal.getDetails());
    }

    public void storeSearchOutcome(
            AddressSearchContext ctx,
            SourceSearchOutcome outcome
    ) {
        TimelineElementInternal timelineElement = timelineUtils.buildAvailabilitySourceTimelineElement(ctx.recipientIndex(), ctx.notification(), outcome.source(), outcome.found(), ctx.attempt(), outcome.address(), outcome.tosAccepted(), ctx.channel().name());
        timelineService.addTimelineElement(timelineElement, ctx.notification());
    }

    public void scheduleSendChannelMessageAction(AddressSearchContext ctx, DigitalAddressSourceInt source) {
        schedulerService.scheduleEvent(
                ctx.notification().getIun(),
                ctx.recipientIndex(),
                Instant.now(),
                ActionType.SEND_CHANNEL_MESSAGE,
                SendChannelMessageDetails.builder()
                        .channel(ctx.channel())
                        .addressSource(source)
                        .build()
        );
    }

    public @NotNull InformalDigitalAddressInt retrieveDigitalAddressFromTimeline(NotificationInt notification, int recIndex, DigitalAddressSourceInt addressSource,
                                                                                 DigitalChannelsInt channel, Integer attempt) {
        String eventId = TimelineUtils.buildGetAddressEventId(notification.getIun(), recIndex, channel.name(), addressSource, attempt);
        TimelineElementInternal timelineElement = timelineService.getTimelineElement(notification.getIun(), eventId)
                .orElseThrow(() -> new PnNotFoundException(
                        "Timeline element not found",
                        String.format("Timeline element not found for iun=%s recIndex=%d addressSource=%s channel=%s",
                                notification.getIun(), recIndex, addressSource, channel),
                        ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT
                ));
        log.info("Timeline element found for iun={} recIndex={} addressSource={} channel={}",
                notification.getIun(), recIndex, addressSource, channel);
        InformalDigitalAddressRelatedTimelineElement details =
                (InformalDigitalAddressRelatedTimelineElement) timelineElement.getDetails();
        if (details.getDigitalAddress() == null) {
            throw new PnNotFoundException(
                    "Digital address not found",
                    String.format("Digital address not found in timeline details for iun=%s recIndex=%d addressSource=%s channel=%s",
                            notification.getIun(), recIndex, addressSource, channel),
                    ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT
            );
        }
        return details.getDigitalAddress();
    }

}
