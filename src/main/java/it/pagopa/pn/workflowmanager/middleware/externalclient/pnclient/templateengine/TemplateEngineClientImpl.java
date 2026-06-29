package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@CustomLog
@RequiredArgsConstructor
@Component
public class TemplateEngineClientImpl implements TemplateEngineClient {
    private final TemplateApi templateApi;

    public String ioMessageTemplate(LanguageEnum language, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, IO_MESSAGE_TEMPLATE);
        return templateApi.informalIoCommunication(language, informalCommunication);
    }
}
