package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.GetAddressInfoDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressSearchUtilsTest {

    @Mock
    private TimelineService timelineService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private SchedulerService schedulerService;

    private AddressSearchUtils utils;

    @BeforeEach
    void setUp() {
        utils = new AddressSearchUtils(timelineService, timelineUtils, schedulerService);
    }

    @Test
    void findPreviousSearchOutcomeReturnsTimelineDetailsWhenPresent() {
        GetAddressInfoDetailsInt expectedDetails = GetAddressInfoDetailsInt.builder()
                .recIndex(1)
                .digitalAddressSource(DigitalAddressSourceInt.PLATFORM)
                .isAvailable(Boolean.TRUE)
                .build();
        String iun = "IUN-UTILS-001";
        String expectedTimelineId = TimelineEventId.GET_ADDRESS.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(1)
                        .source(DigitalAddressSourceInt.PLATFORM)
                        .sentAttemptMade(4)
                        .channel(ChannelType.PEC.name())
                        .build()
        );
        when(timelineService.getTimelineElement(iun, expectedTimelineId))
                .thenReturn(Optional.of(TimelineElementInternal.builder().details(expectedDetails).build()));

        Optional<GetAddressInfoDetailsInt> result = utils.findPreviousSearchOutcome(
                iun,
                1,
                DigitalAddressSourceInt.PLATFORM,
                ChannelType.PEC,
                4
        );

        assertThat(result).containsSame(expectedDetails);
    }

    @Test
    void storeSearchOutcomeBuildsAndPersistsTimelineElement() {
        AddressSearchContext context = buildContext();
        SourceSearchOutcome outcome = SourceSearchOutcome.found(DigitalAddressSourceInt.SPECIAL, null);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().elementId("timeline-id").build();
        when(timelineUtils.buildAvailabilitySourceTimelineElement(
                context.recipientIndex(),
                context.notification(),
                DigitalAddressSourceInt.SPECIAL,
                true,
                context.attempt()
        )).thenReturn(timelineElement);

        utils.storeSearchOutcome(context, outcome);

        verify(timelineService).addTimelineElement(timelineElement, context.notification());
    }

    @Test
    void scheduleSendChannelMessageActionSchedulesExpectedPayload() {
        AddressSearchContext context = buildContext();
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<SendChannelMessageDetails> detailsCaptor = ArgumentCaptor.forClass(SendChannelMessageDetails.class);

        utils.scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.GENERAL);

        verify(schedulerService).scheduleEvent(
                eq(context.notification().getIun()),
                eq(context.recipientIndex()),
                instantCaptor.capture(),
                eq(ActionType.SEND_CHANNEL_MESSAGE),
                detailsCaptor.capture()
        );
        assertThat(instantCaptor.getValue()).isNotNull();
        assertThat(detailsCaptor.getValue().getChannel()).isEqualTo(ChannelType.PEC);
        assertThat(detailsCaptor.getValue().getAddressSource()).isEqualTo(DigitalAddressSourceInt.GENERAL);
    }

    private static AddressSearchContext buildContext() {
        Instant sentAt = Instant.parse("2026-04-01T09:30:00Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-UTILS-CTX")
                .sentAt(sentAt)
                .build();
        return new AddressSearchContext(ChannelType.PEC, sentAt, notification, 1, 2);
    }
}
