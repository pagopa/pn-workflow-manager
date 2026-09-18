package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.ApiClient;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.api.AddressApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NationalRegistriesApiConfigurator {

    @Bean
    @Primary
    public AddressApi addressApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg) {
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getNationalRegistriesBaseUrl());
        return new AddressApi(newApiClient);
    }
}
