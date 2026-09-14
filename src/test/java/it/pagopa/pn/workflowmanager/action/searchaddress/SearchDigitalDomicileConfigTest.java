package it.pagopa.pn.workflowmanager.action.searchaddress;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchDigitalDomicileConfigTest {

    @Test
    void shouldExposeAllFields() {
        Instant validFrom = Instant.parse("2026-10-01T00:00:00Z");
        SearchDigitalDomicileConfig config = new SearchDigitalDomicileConfig(
                validFrom,
                List.of(DigitalAddressSourceInt.PLATFORM),
                List.of(DigitalAddressSourceInt.SPECIAL),
                List.of(DigitalAddressSourceInt.GENERAL)
        );

        assertThat(config.getValidFrom()).isEqualTo(validFrom);
        assertThat(config.getPec()).containsExactly(DigitalAddressSourceInt.PLATFORM);
        assertThat(config.getSms()).containsExactly(DigitalAddressSourceInt.SPECIAL);
        assertThat(config.getEmail()).containsExactly(DigitalAddressSourceInt.GENERAL);
    }
}
