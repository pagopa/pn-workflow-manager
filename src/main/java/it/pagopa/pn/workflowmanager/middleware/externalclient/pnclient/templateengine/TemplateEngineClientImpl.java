package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.File;

@CustomLog
@RequiredArgsConstructor
@Component
public class TemplateEngineClientImpl implements TemplateEngineClient {
    private final TemplateApi templateApi;

    @Override
    public String ioMessageTemplate(LanguageEnum language, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, IO_MESSAGE_TEMPLATE);
        return templateApi.informalIoCommunication(language, informalCommunication);
    }

    @Override
    public File informalAnalogCommunication(LanguageEnum xLanguage, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, IO_MESSAGE_TEMPLATE);
        return templateApi.informalAnalogCommunication(xLanguage, informalCommunication);
    }

    @Override
    public String pecTemplate(LanguageEnum language, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, PEC_TEMPLATE);
        return templateApi.informalPecCommunication(language, informalCommunication);
    }

    @Override
    public String informalIoCommunication(LanguageEnum xLanguage, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, INFORMAL_IO_COMMUNICATION);
        return templateApi.informalIoCommunication(xLanguage, informalCommunication);
    }
}
