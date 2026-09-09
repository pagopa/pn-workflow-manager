package it.pagopa.pn.workflowmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.workflowmanager.dto.ChannelSourceRule;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.consent.ConsentDto;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
    private String userAttributesBaseUrl;

    private String nationalRegistriesBaseUrl;

    private Integer ioPollingMaxMins;
    private Map<ChannelType, List<ChannelSourceRule>> addressSearchMap = new EnumMap<>(ChannelType.class);
    List<ConsentDto> consentsForPlatformSearch;

    private Map<ChannelType, List<ChannelAddressSourceRule>> addressSearchMap = new EnumMap<>(ChannelType.class);

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
