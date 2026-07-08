package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalEmailCommunicationSubject;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalSmsCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public String pecBodyTemplate(LanguageEnum language, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, PEC_BODY_TEMPLATE);
        return templateApi.informalPecCommunicationBody(language, informalCommunication);
    }

    @Override
    public String pecSubjectTemplate(LanguageEnum language, InformalEmailCommunicationSubject informalCommunicationSubject) {
        log.logInvokingExternalService(CLIENT_NAME, PEC_SUBJECT_TEMPLATE);
        return templateApi.informalPecCommunicationSubject(language, informalCommunicationSubject);
    }

    @Override
    public String emailBodyTemplate(LanguageEnum language, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, EMAIL_BODY_TEMPLATE);
        return templateApi.informalEmailCommunicationBody(language, informalCommunication);
    }

    @Override
    public String emailSubjectTemplate(LanguageEnum language, InformalEmailCommunicationSubject informalCommunicationSubject) {
        log.logInvokingExternalService(CLIENT_NAME, EMAIL_SUBJECT_TEMPLATE);
        return templateApi.informalEmailCommunicationSubject(language, informalCommunicationSubject);
    }

    @Override
    public File coverpageTemplate(LanguageEnum xLanguage, InformalCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, COVERPAGE_TEMPLATE);
        return templateApi.informalAnalogCommunication(xLanguage, informalCommunication);
    }

    @Override
    public String smsTemplate(LanguageEnum language, InformalSmsCommunication informalCommunication) {
        log.logInvokingExternalService(CLIENT_NAME, SMS_TEMPLATE);
        return templateApi.informalSmsCommunication(language, informalCommunication);
    }
}
