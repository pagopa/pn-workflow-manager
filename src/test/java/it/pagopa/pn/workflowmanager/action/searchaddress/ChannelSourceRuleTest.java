package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelSourceRuleTest {

    @Test
    void shouldExposeRuleFields() {
        Instant validFrom = Instant.parse("2026-02-01T00:00:00Z");
        List<DigitalAddressSourceInt> sources = List.of(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL);

        ChannelSourceRule rule = new ChannelSourceRule(validFrom, sources);

        assertThat(rule.validFrom()).isEqualTo(validFrom);
        assertThat(rule.sources()).containsExactly(DigitalAddressSourceInt.PLATFORM, DigitalAddressSourceInt.SPECIAL);
    }
}
