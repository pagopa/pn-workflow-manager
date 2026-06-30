package it.pagopa.pn.workflowmanager.config;

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
    private String cxId;
    private String deliveryBaseUrl;
    private String timelineClientBaseUrl;
    private String ioConnectorBaseUrl;

    private Integer ioPollingMaxMins;

    @Data
    public static class Topics {
        private String actionQueue;
        private String digitalQueue;
        private String analogQueue;
        private String ioQueue;
    }

    @PostConstruct
    public void init() {
        log.info("PnWorkflowManagerConfigs={}", this);
    }
}
