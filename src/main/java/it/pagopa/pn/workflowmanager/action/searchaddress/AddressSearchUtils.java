package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.GetAddressInfoDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class AddressSearchUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final SchedulerService schedulerService;

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
        TimelineElementInternal timelineElement = timelineUtils.buildAvailabilitySourceTimelineElement(ctx.recipientIndex(), ctx.notification(), outcome.source(), outcome.found(), ctx.attempt());
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


}
