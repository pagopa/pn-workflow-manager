package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalEmailCommunicationSubject;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalSmsCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine.TemplateEngineClient;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static it.pagopa.pn.workflowmanager.service.mapper.TemplateEngineMapper.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateGeneratorServiceImpl implements TemplateGeneratorService {
    private final TemplateEngineClient templateEngineClient;

    @Override
    public String generateIoMessageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, campaign);
        return templateEngineClient.ioMessageTemplate(language, informalCommunication);
    }

    @Override
    public String generatePecBodyTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, campaign);
        return templateEngineClient.pecBodyTemplate(language, informalCommunication);
    }

    @Override
    public String generatePecSubjectTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalEmailCommunicationSubject informalEmailCommunicationSubject = mapToInformalEmailCommunicationSubject(notificationInt, notificationRecipientInt);
        return templateEngineClient.pecSubjectTemplate(language, informalEmailCommunicationSubject);
    }

    @Override
    public String generateEmailBodyTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, campaign);
        return templateEngineClient.emailBodyTemplate(language, informalCommunication);
    }

    @Override
    public String generateEmailSubjectTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalEmailCommunicationSubject informalEmailCommunicationSubject = mapToInformalEmailCommunicationSubject(notificationInt, notificationRecipientInt);
        return templateEngineClient.emailSubjectTemplate(language, informalEmailCommunicationSubject);
    }

    @Override
    public byte[] generateCoverpageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, campaign);
        return templateEngineClient.coverpageTemplate(language, informalCommunication);
    }

    @Override
    public String generateSmsTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalSmsCommunication informalSmsCommunication = mapToInformalSmsCommunication(notificationInt, notificationRecipientInt);
        return templateEngineClient.smsTemplate(language, informalSmsCommunication);
    }

    private LanguageEnum getLanguage(List<String> additionalLanguages) {
        return CollectionUtils.isEmpty(additionalLanguages)
                ? LanguageEnum.IT : LanguageEnum.fromValue(additionalLanguages.getFirst());
    }
}
