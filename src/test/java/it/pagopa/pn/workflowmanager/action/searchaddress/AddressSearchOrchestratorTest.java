package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressSearchOrchestratorTest {

    @Mock
    private ChannelAddressSourceConfigResolver configResolver;
    @Mock
    private AddressSearchProgressor progressor;
    @Mock
    private AddressSearchUtils searchUtils;

    private AddressSearchOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new AddressSearchOrchestrator(configResolver, progressor, searchUtils);
    }

    @Test
    void handleDelegatesToProgressorWithConfiguredSources() {
        AddressSearchContext context = buildContext();
        List<DigitalAddressSourceInt> sources = List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL);
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(sources);

        orchestrator.start(context);

        verify(progressor).run(context, sources, 0);
        verifyNoInteractions(searchUtils);
    }

    @Test
    void handleDelegatesToProgressorWithSpecialWhenNoSourcesAreConfigured() {
        AddressSearchContext context = buildContext();
        List<DigitalAddressSourceInt> sources = List.of(DigitalAddressSourceInt.SPECIAL);
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(sources);

        orchestrator.start(context);

        verify(progressor).run(context, sources, 0);
        verifyNoInteractions(searchUtils);
    }

    @ParameterizedTest
    @CsvSource({
            "IO",
            "ANALOG"
    })
    void handleSchedulesSendChannelMessageWhenChannelDoesntNeedSearches(ChannelType channel) {
        Instant sentAt = Instant.parse("2026-03-10T11:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-ORCH")
                .sentAt(sentAt)
                .build();
        AddressSearchContext context = new AddressSearchContext(channel, sentAt, notification, 0, 1);

        orchestrator.start(context);

        verify(searchUtils).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.SPECIAL);
        verifyNoInteractions(progressor);
        verifyNoInteractions(configResolver);
    }

    @Test
    void resumeDelegatesToProgressorWithResolvedSourcesWhenChannelIsPec() {
        AddressSearchContext context = buildContext();
        List<DigitalAddressSourceInt> sources = List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL);
        SourceSearchOutcome outcome = SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(sources);

        orchestrator.resume(context, DigitalAddressSourceInt.PLATFORM, outcome);

        verify(progressor).resumeAfterAsyncOutcome(context, DigitalAddressSourceInt.PLATFORM, outcome, sources);
        verifyNoInteractions(searchUtils);
    }

    @Test
    void resumeUsesDefaultSpecialSourceWhenNoSourcesAreConfigured() {
        AddressSearchContext context = buildContext();
        List<DigitalAddressSourceInt> defaultSources = List.of(DigitalAddressSourceInt.SPECIAL);
        SourceSearchOutcome outcome = SourceSearchOutcome.notFound(DigitalAddressSourceInt.SPECIAL);
        when(configResolver.resolveSources(ChannelType.PEC, context.sentAt())).thenReturn(List.of(DigitalAddressSourceInt.SPECIAL));

        orchestrator.resume(context, DigitalAddressSourceInt.SPECIAL, outcome);

        verify(progressor).resumeAfterAsyncOutcome(context, DigitalAddressSourceInt.SPECIAL, outcome, defaultSources);
        verifyNoInteractions(searchUtils);
    }

    private static AddressSearchContext buildContext() {
        Instant sentAt = Instant.parse("2026-03-10T11:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-ORCH")
                .sentAt(sentAt)
                .build();
        return new AddressSearchContext(ChannelType.PEC, sentAt, notification, 0, 1);
    }
}
