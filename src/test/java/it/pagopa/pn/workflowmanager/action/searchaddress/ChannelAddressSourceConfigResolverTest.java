package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChannelAddressSourceConfigResolverTest {

    @Test
    void validateRulesThrowsWhenRuleSourcesAreEmpty() {
        PnWorkflowManagerConfigs configs = buildConfigs(Map.of(
                ChannelType.EMAIL, List.of(rule("2024-01-01T00:00:00Z", List.of()))
        ));
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(configs);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Channel EMAIL has a rule with empty or null sources for validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesThrowsWhenTwoRulesHaveSameValidFrom() {
        Instant validFrom = Instant.parse("2024-01-01T00:00:00Z");
        PnWorkflowManagerConfigs configs = buildConfigs(Map.of(
                ChannelType.PEC, List.of(
                        new ChannelSourceRule(validFrom, List.of(DigitalAddressSourceInt.PLATFORM)),
                        new ChannelSourceRule(validFrom, List.of(DigitalAddressSourceInt.SPECIAL))
                )
        ));
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(configs);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, resolver::validateRules);

        assertThat(exception.getMessage())
                .isEqualTo("Channel PEC has multiple rules with the same validFrom 2024-01-01T00:00:00Z");
    }

    @Test
    void validateRulesDoesNotThrowWhenRulesAreValid() {
        PnWorkflowManagerConfigs configs = buildConfigs(Map.of(
                ChannelType.SMS, List.of(
                        rule("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM)),
                        rule("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL, DigitalAddressSourceInt.GENERAL))
                )
        ));
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(configs);

        assertDoesNotThrow(resolver::validateRules);
    }

    @Test
    void resolveSourcesReturnsLatestRuleBeforeSentAt() {
        PnWorkflowManagerConfigs configs = buildConfigs(Map.of(
                ChannelType.IO, List.of(
                        rule("2024-01-01T00:00:00Z", List.of(DigitalAddressSourceInt.PLATFORM)),
                        rule("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL)),
                        rule("2024-03-01T00:00:00Z", List.of(DigitalAddressSourceInt.GENERAL))
                )
        ));
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(configs);

        Optional<List<DigitalAddressSourceInt>> sources = resolver.resolveSources(ChannelType.IO, Instant.parse("2024-02-15T00:00:00Z"));

        assertThat(sources).contains(List.of(DigitalAddressSourceInt.SPECIAL));
    }

    @Test
    void resolveSourcesReturnsEmptyWhenNoRuleMatchesSentAt() {
        PnWorkflowManagerConfigs configs = buildConfigs(Map.of(
                ChannelType.IO, List.of(
                        rule("2024-02-01T00:00:00Z", List.of(DigitalAddressSourceInt.SPECIAL))
                )
        ));
        ChannelAddressSourceConfigResolver resolver = new ChannelAddressSourceConfigResolver(configs);

        Optional<List<DigitalAddressSourceInt>> sources = resolver.resolveSources(ChannelType.IO, Instant.parse("2024-01-15T00:00:00Z"));

        assertThat(sources).isEmpty();
    }

    private static PnWorkflowManagerConfigs buildConfigs(Map<ChannelType, List<ChannelSourceRule>> rulesByChannel) {
        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        configs.setAddressSearchMap(new EnumMap<>(rulesByChannel));
        return configs;
    }

    private static ChannelSourceRule rule(String validFrom, List<DigitalAddressSourceInt> sources) {
        return new ChannelSourceRule(Instant.parse(validFrom), sources);
    }
}
