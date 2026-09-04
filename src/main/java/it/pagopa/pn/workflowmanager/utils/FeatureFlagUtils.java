package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;

@AllArgsConstructor
@Component
@CustomLog
public class FeatureFlagUtils {

    private final PnWorkflowManagerConfigs configs;

    public boolean isDigitalDomicileSearchEnabled(Instant sentAt) {
        return configs.getSearchDigitalDomicileStartDate().isBefore(sentAt);
    }
}
