package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.ApiClient;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DeliveryApiConfigurator {
    @Bean
    @Primary
    public InternalOnlyApi internalOnlyApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getDeliveryBaseUrl());
        return new InternalOnlyApi( newApiClient );
    }
}
