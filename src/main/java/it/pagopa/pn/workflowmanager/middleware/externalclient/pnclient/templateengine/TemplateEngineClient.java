package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalEmailCommunicationSubject;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;

import java.io.File;

public interface TemplateEngineClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_TEMPLATE_ENGINE;

    String IO_MESSAGE_TEMPLATE = "IO MESSAGE TEMPLATE";
    String PEC_BODY_TEMPLATE = "PEC BODY TEMPLATE";
    String PEC_SUBJECT_TEMPLATE = "PEC SUBJECT TEMPLATE";
    String EMAIL_BODY_TEMPLATE = "EMAIL BODY TEMPLATE";
    String EMAIL_SUBJECT_TEMPLATE = "EMAIL SUBJECT TEMPLATE";
    String COVERPAGE_TEMPLATE = "COVERPAGE TEMPLATE";


    String ioMessageTemplate(LanguageEnum language, InformalCommunication informalCommunication);

    String pecBodyTemplate(LanguageEnum language, InformalCommunication informalCommunication);

    String pecSubjectTemplate(LanguageEnum language, InformalEmailCommunicationSubject informalCommunicationSubject);

    String emailBodyTemplate(LanguageEnum language, InformalCommunication informalCommunication);

    String emailSubjectTemplate(LanguageEnum language, InformalEmailCommunicationSubject informalCommunicationSubject);

    File coverpageTemplate(LanguageEnum xLanguage, InformalCommunication informalCommunication);

}
