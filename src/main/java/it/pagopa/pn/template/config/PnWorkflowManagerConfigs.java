package it.pagopa.pn.template.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties( prefix = "pn.workflow-manager")
@Validated
@Data
@Import({SharedAutoConfiguration.class})
@Slf4j
public class PnWorkflowManagerConfigs {
    private Topics topics;
    private EventBus eventBus;

    @Data
    public static class Topics {
        private String pnWorkflowManagerActionQueue;
    }

    @Data
    public static class EventBus {
        private String name;
        private String source;
        private String outcomeEventDetailType;
    }

    @PostConstruct
    public void init() {
        log.info("PnNotificationCostServiceConfigs={}", this);
    }
}
