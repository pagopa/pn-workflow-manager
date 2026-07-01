package it.pagopa.pn.workflowmanager.config.msclient;


import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.ApiClient;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TemplateEngineApiConfigurator {
    @Bean
    @Primary
    public TemplateApi ioConnectorApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getTemplateEngineBaseUrl());
        return new TemplateApi( newApiClient );
    }
}
