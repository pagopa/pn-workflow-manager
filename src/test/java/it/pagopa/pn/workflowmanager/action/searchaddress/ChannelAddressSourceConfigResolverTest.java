package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.SyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChannelAddressSourceConfigResolverTest {

    @Test
    void validateRulesThrowsWhenPecIsNull() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", null, List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Search digital domicile configuration has null pec sources for validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesThrowsWhenTwoConfigsHaveSameValidFrom() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM), List.of(), List.of()),
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL), List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Multiple search digital domicile configurations have the same validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesThrowsWhenChannelHasDuplicateSource() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL, DigitalAddressSourceInt.SPECIAL), List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Channel PEC has duplicated source SPECIAL for validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesThrowsWhenChannelHasUnsupportedSource() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.GENERAL), List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Channel PEC has unsupported source GENERAL for validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesDoesNotThrowWhenRulesAreValid() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM), List.of(), List.of()),
                config("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL), List.of(DigitalAddressSourceInt.GENERAL), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        assertDoesNotThrow(resolver::validateRules);
    }

    @Test
    void validateRulesDoesNotThrowWhenConfiguredSourcesAreSupported() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z",
                        List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL, DigitalAddressSourceInt.GENERAL),
                        List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL),
                        List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL))
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        assertDoesNotThrow(resolver::validateRules);
    }

    @Test
    void resolveSourcesReturnsLatestRuleBeforeSentAt() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM), List.of(), List.of(DigitalAddressSourceInt.PLATFORM)),
                config("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL), List.of(), List.of(DigitalAddressSourceInt.SPECIAL)),
                config("2024-03-01T00:00:00Z", List.of(DigitalAddressSourceInt.GENERAL), List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        Optional<List<DigitalAddressSourceInt>> sources = resolver.resolveSources(ChannelType.EMAIL, Instant.parse("2024-02-15T00:00:00Z"));

        assertThat(sources).contains(List.of(DigitalAddressSourceInt.SPECIAL));
    }

    @Test
    void resolveSourcesReturnsEmptyWhenNoRuleMatchesSentAt() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL), List.of(), List.of())
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        Optional<List<DigitalAddressSourceInt>> sources = resolver.resolveSources(ChannelType.PEC, Instant.parse("2024-01-15T00:00:00Z"));

        assertThat(sources).isEmpty();
    }

    @Test
    void resolveSourcesReturnsEmptyListForUnsupportedChannel() {
        SearchDigitalDomicileParameterConsumer parameterConsumer = mockConsumer(
                config("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM), List.of(DigitalAddressSourceInt.SPECIAL), List.of(DigitalAddressSourceInt.GENERAL))
        );
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(parameterConsumer, supportedStrategies());

        Optional<List<DigitalAddressSourceInt>> sources = resolver.resolveSources(ChannelType.IO, Instant.parse("2024-02-15T00:00:00Z"));

        assertThat(sources).contains(List.of());
    }

    private static SearchDigitalDomicileParameterConsumer mockConsumer(SearchDigitalDomicileConfig... configs) {
        SearchDigitalDomicileParameterConsumer parameterConsumer = Mockito.mock(SearchDigitalDomicileParameterConsumer.class);
        Mockito.when(parameterConsumer.getSearchDigitalDomicileConfigs()).thenReturn(List.of(configs));
        return parameterConsumer;
    }

    private static List<AddressSearchStrategy> supportedStrategies() {
        return List.of(
                new TestSyncStrategy(Set.of(
                        new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.PEC),
                        new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.SMS),
                        new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.EMAIL)
                )),
                new TestSyncStrategy(Set.of(
                        new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.PEC),
                        new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.SMS),
                        new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.EMAIL)
                )),
                new TestSyncStrategy(Set.of(
                        new SourceChannelKey(DigitalAddressSourceInt.GENERAL, ChannelType.PEC)
                ))
        );
    }

    private static SearchDigitalDomicileConfig config(String validFrom,
                                                      List<DigitalAddressSourceInt> pec,
                                                      List<DigitalAddressSourceInt> sms,
                                                      List<DigitalAddressSourceInt> email) {
        return new SearchDigitalDomicileConfig(Instant.parse(validFrom), pec, sms, email);
    }

    private static final class TestSyncStrategy implements SyncAddressSearchStrategy {
        private final Set<SourceChannelKey> supportedKeys;

        private TestSyncStrategy(Set<SourceChannelKey> supportedKeys) {
            this.supportedKeys = supportedKeys;
        }

        @Override
        public Set<SourceChannelKey> supportedKeys() {
            return supportedKeys;
        }

        @Override
        public SourceSearchOutcome search(AddressSearchContext context) {
            throw new UnsupportedOperationException("Not needed for this test");
        }
    }
}
