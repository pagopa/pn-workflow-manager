package it.pagopa.pn.workflowmanager.config.msclient;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.ApiClient;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.api.PaperMessagesApi;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PaperMessagesApiConfigurator {
    @Bean
    @Primary
    public PaperMessagesApi paperMessagesApi(@Qualifier("withTracing") RestTemplate restTemplate, PnWorkflowManagerConfigs cfg){
        ApiClient newApiClient = new ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getPaperMessagesClientBaseUrl());
        return new PaperMessagesApi( newApiClient );
    }
}
