package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.ApiClient;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileUploadApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SafeStorageApiConfigurator {
    @Bean
    @Primary
    public FileUploadApi fileUploadApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getSafeStorageBaseUrl());
        return new FileUploadApi( newApiClient );
    }

    @Bean
    @Primary
    public FileDownloadApi fileDownloadApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getSafeStorageBaseUrl());
        return new FileDownloadApi( newApiClient );
    }

}
