package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;

public interface TemplateEngineClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_TEMPLATE_ENGINE;

    String IO_MESSAGE_TEMPLATE = "IO MESSAGE TEMPLATE";
    String PEC_TEMPLATE = "PEC TEMPLATE";
    String INFORMAL_IO_COMMUNICATION = "IO INFORMAL COMMUNICATION";

    String ioMessageTemplate(LanguageEnum language, InformalCommunication informalCommunication);

    String pecTemplate(LanguageEnum language, InformalCommunication informalCommunication);

    String informalIoCommunication(LanguageEnum xLanguage, InformalCommunication informalCommunication);

}
