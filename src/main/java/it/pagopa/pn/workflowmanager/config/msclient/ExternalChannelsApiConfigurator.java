package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.ApiClient;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalLegalMessagesApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ExternalChannelsApiConfigurator {
    @Bean
    @Primary
    public DigitalLegalMessagesApi digitalLegalMessagesApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getExternalChannelsBaseUrl());
        return new DigitalLegalMessagesApi( newApiClient );
    }
}
