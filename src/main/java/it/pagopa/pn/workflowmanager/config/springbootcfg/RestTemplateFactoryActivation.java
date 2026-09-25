package it.pagopa.pn.workflowmanager.config.springbootcfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.pnclients.RestTemplateFactory;
import it.pagopa.pn.commons.pnclients.RestTemplateRetryable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateFactoryActivation extends RestTemplateFactory {
    @Bean
    @Qualifier("withJavaTimeModule")
    public RestTemplate restTemplateWithOffsetDateTimeFormatter(
            @Value("${pn.commons.retry.max-attempts}") int retryMaxAttempts,
            @Value("${pn.commons.connection-timeout-millis}") int connectionTimeout,
            @Value("${pn.commons.read-timeout-millis}") int readTimeout,
            ObjectMapper objectMapper
    ) {
        RestTemplate template = new RestTemplateRetryable(retryMaxAttempts + 1);
        this.configureRestTemplate(connectionTimeout, readTimeout, template);
        ObjectMapper customObjectMapper = objectMapper.copy();
        customObjectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        template.getMessageConverters().stream()
                .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
                .map(AbstractJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.setObjectMapper(customObjectMapper));

        return template;
    }
}