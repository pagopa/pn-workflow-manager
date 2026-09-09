package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
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
        // Cerca su timeline usando iun e timelineElementId (da costruire).
        return Optional.empty();
    }

    public void storeSearchOutcome(
            AddressSearchContext ctx,
            SourceSearchOutcome outcome
    ) {
        // Store the search outcome in the timeline or database
    }

    public void scheduleSendChannelMessageAction(AddressSearchContext ctx, DigitalAddressSourceInt source) {
        schedulerService.scheduleEvent(
                ctx.iun(),
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
