package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;

import java.io.File;

public interface TemplateEngineClient {
    // TODO: String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_TEMPLATE_ENGINE;
    String CLIENT_NAME = "pn-template-engine";

    String IO_MESSAGE_TEMPLATE = "IO MESSAGE TEMPLATE";

    String ioMessageTemplate(LanguageEnum language, InformalCommunication informalCommunication);
    File informalAnalogCommunication(LanguageEnum xLanguage, InformalCommunication informalCommunication);
}
