package it.pagopa.pn.workflowmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;

@Configuration
@ConfigurationProperties( prefix = "pn.workflow-manager")
@Validated
@Data
@Import({SharedAutoConfiguration.class})
@Slf4j
public class PnWorkflowManagerConfigs {
    private Topics topics;
    private String cxId;
    private List<String> pnSendMode;
    private PaperChannel paperChannel;
    //external client
    private String timelineClientBaseUrl;
    private String actionManagerBaseUrl;
    private String deliveryBaseUrl;
    private String templateEngineBaseUrl;
    private String ioConnectorBaseUrl;
    private String safeStorageBaseUrl;
    private String paperMessagesClientBaseUrl;
    private String externalChannelsBaseUrl;

    private Integer ioPollingMaxMins;
    private Instant searchDigitalDomicileStartDate;

    @Data
    public static class Topics {
        private String actionQueue;
        private String digitalQueue;
        private String analogQueue;
        private String ioQueue;
        private String safeStorageEvents;
        private String informalQueue;
    }

    @Data
    public static class SenderAddress {
        private String fullname;
        private String address;
        private String zipcode;
        private String city;
        private String pr;
        private String country;
    }

    @Data
    public static class PaperChannel {

        private SenderAddress senderAddress;

        public PhysicalAddressInt getSenderPhysicalAddress(){
            return PhysicalAddressInt.builder()
                    .fullname(senderAddress.getFullname())
                    .address(senderAddress.getAddress())
                    .zip(senderAddress.getZipcode())
                    .province(senderAddress.getPr())
                    .municipality(senderAddress.getCity())
                    .foreignState(senderAddress.getCountry())
                    .build();
        }
    }

    @PostConstruct
    public void init() {
        log.info("PnWorkflowManagerConfigs={}", this);
    }
}
