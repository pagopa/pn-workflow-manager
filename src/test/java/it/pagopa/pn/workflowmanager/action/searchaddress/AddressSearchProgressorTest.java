package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AsyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.SyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.GetAddressInfoDetailsInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressSearchProgressorTest {

    @Mock
    private AddressSearchUtils utils;
    @Mock
    private AddressSearchRegistry registry;

    private AddressSearchProgressor progressor;

    @BeforeEach
    void setUp() {
        progressor = new AddressSearchProgressor(utils, registry);
    }

    @Test
    void runSchedulesExistingAvailableSourceAndStops() {
        AddressSearchContext context = buildContext();
        when(utils.findPreviousSearchOutcome(context.notification().getIun(), context.recipientIndex(), DigitalAddressSourceInt.PLATFORM, context.channel(), context.attempt()))
                .thenReturn(Optional.of(GetAddressInfoDetailsInt.builder().isAvailable(Boolean.TRUE).build()));

        progressor.run(context, List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL), 0);

        verify(utils).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.PLATFORM);
        verifyNoInteractions(registry);
    }

    @Test
    void runSkipsUnavailableExistingSourceAndContinuesToNextOne() {
        AddressSearchContext context = buildContext();
        TestSyncStrategy strategy = new TestSyncStrategy(SourceSearchOutcome.found(DigitalAddressSourceInt.SPECIAL, null), ChannelType.PEC, DigitalAddressSourceInt.SPECIAL);
        when(utils.findPreviousSearchOutcome(context.notification().getIun(), context.recipientIndex(), DigitalAddressSourceInt.PLATFORM, context.channel(), context.attempt()))
                .thenReturn(Optional.of(GetAddressInfoDetailsInt.builder().isAvailable(Boolean.FALSE).build()));
        when(utils.findPreviousSearchOutcome(context.notification().getIun(), context.recipientIndex(), DigitalAddressSourceInt.SPECIAL, context.channel(), context.attempt()))
                .thenReturn(Optional.empty());
        when(registry.find(DigitalAddressSourceInt.SPECIAL, ChannelType.PEC)).thenReturn(strategy);

        progressor.run(context, List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL), 0);

        verify(utils).storeSearchOutcome(context, strategy.outcome);
        verify(utils).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.SPECIAL);
    }

    @Test
    void runStoresNotFoundSyncOutcomeAndSchedulesNoneWhenPlanEnds() {
        AddressSearchContext context = buildContext();
        TestSyncStrategy strategy = new TestSyncStrategy(SourceSearchOutcome.notFound(DigitalAddressSourceInt.GENERAL), ChannelType.PEC, DigitalAddressSourceInt.GENERAL);
        when(utils.findPreviousSearchOutcome(context.notification().getIun(), context.recipientIndex(), DigitalAddressSourceInt.GENERAL, context.channel(), context.attempt()))
                .thenReturn(Optional.empty());
        when(registry.find(DigitalAddressSourceInt.GENERAL, ChannelType.PEC)).thenReturn(strategy);

        progressor.run(context, List.of(DigitalAddressSourceInt.GENERAL), 0);

        verify(utils).storeSearchOutcome(context, strategy.outcome);
        verify(utils).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.NONE);
    }

    @Test
    void runTriggersAsyncSearchAndStops() {
        AddressSearchContext context = buildContext();
        TestAsyncStrategy strategy = spy(new TestAsyncStrategy(ChannelType.PEC, DigitalAddressSourceInt.GENERAL));
        when(utils.findPreviousSearchOutcome(context.notification().getIun(), context.recipientIndex(), DigitalAddressSourceInt.GENERAL, context.channel(), context.attempt()))
                .thenReturn(Optional.empty());
        when(registry.find(DigitalAddressSourceInt.GENERAL, ChannelType.PEC)).thenReturn(strategy);

        progressor.run(context, List.of(DigitalAddressSourceInt.GENERAL, DigitalAddressSourceInt.SPECIAL), 0);

        verify(strategy).triggerSearch(context);
        verify(utils, never()).scheduleSendChannelMessageAction(context, DigitalAddressSourceInt.NONE);
        verify(utils, never()).storeSearchOutcome(any(), any());
    }

    private static AddressSearchContext buildContext() {
        Instant sentAt = Instant.parse("2026-05-05T12:00:00Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-PROGRESSOR")
                .sentAt(sentAt)
                .build();
        return new AddressSearchContext(ChannelType.PEC, sentAt, notification, 0, 1);
    }

    private static final class TestSyncStrategy implements SyncAddressSearchStrategy {
        private final SourceSearchOutcome outcome;
        private final Set<SourceChannelKey> supportedKeys;

        private TestSyncStrategy(SourceSearchOutcome outcome, ChannelType channelType, DigitalAddressSourceInt source) {
            this.outcome = outcome;
            this.supportedKeys = Set.of(new SourceChannelKey(source, channelType));
        }

        @Override
        public SourceSearchOutcome search(AddressSearchContext context) {
            return outcome;
        }

        @Override
        public Set<SourceChannelKey> supportedKeys() {
            return supportedKeys;
        }
    }

    private static class TestAsyncStrategy implements AsyncAddressSearchStrategy {
        private final Set<SourceChannelKey> supportedKeys;

        private TestAsyncStrategy(ChannelType channelType, DigitalAddressSourceInt source) {
            this.supportedKeys = Set.of(new SourceChannelKey(source, channelType));
        }

        @Override
        public Set<SourceChannelKey> supportedKeys() {
            return supportedKeys;
        }

        @Override
        public void triggerSearch(AddressSearchContext context) {
        }
    }
}
