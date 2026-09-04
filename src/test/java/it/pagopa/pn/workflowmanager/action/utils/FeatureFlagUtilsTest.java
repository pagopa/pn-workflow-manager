package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.utils.FeatureFlagUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagUtilsTest {

    @Mock
    private PnWorkflowManagerConfigs configs;

    private FeatureFlagUtils featureFlagUtils;

    @BeforeEach
    void setUp() {
        featureFlagUtils = new FeatureFlagUtils(configs);
    }

    @Test
    void isDigitalDomicileSearchEnabled_whenSentAtIsAfterStartDate_shouldReturnTrue() {
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = startDate.plusSeconds(1);

        when(configs.getSearchDigitalDomicileStartDate()).thenReturn(startDate);

        assertTrue(featureFlagUtils.isDigitalDomicileSearchEnabled(sentAt));
    }

    @Test
    void isDigitalDomicileSearchEnabled_whenSentAtEqualsStartDate_shouldReturnFalse() {
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");

        when(configs.getSearchDigitalDomicileStartDate()).thenReturn(startDate);

        assertFalse(featureFlagUtils.isDigitalDomicileSearchEnabled(startDate));
    }

    @Test
    void isDigitalDomicileSearchEnabled_whenSentAtIsBeforeStartDate_shouldReturnFalse() {
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant sentAt = startDate.minusSeconds(1);

        when(configs.getSearchDigitalDomicileStartDate()).thenReturn(startDate);

        assertFalse(featureFlagUtils.isDigitalDomicileSearchEnabled(sentAt));
    }

    @Test
    void isDigitalDomicileSearchEnabled_whenStartDateIsNull_shouldThrowNullPointerException() {
        Instant sentAt = Instant.parse("2024-01-01T00:00:00Z");

        when(configs.getSearchDigitalDomicileStartDate()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> featureFlagUtils.isDigitalDomicileSearchEnabled(sentAt));
    }
}
