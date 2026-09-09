package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.ApiClient;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.ConsentsApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.CourtesyApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.LegalApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserAttributesApiConfigurator {

    @Bean
    @Primary
    public LegalApi legalApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg) {
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getUserAttributesBaseUrl());
        return new LegalApi(newApiClient);
    }

    @Bean
    @Primary
    public CourtesyApi courtesyApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg) {
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getUserAttributesBaseUrl());
        return new CourtesyApi(newApiClient);
    }

    @Bean
    @Primary
    public ConsentsApi consentsApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg) {
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getUserAttributesBaseUrl());
        return new ConsentsApi(newApiClient);
    }
}
