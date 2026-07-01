package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine.TemplateEngineClient;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static it.pagopa.pn.workflowmanager.service.mapper.TemplateEngineMapper.mapToInformalCommunication;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateGeneratorServiceImpl implements TemplateGeneratorService {
    private final TemplateEngineClient templateEngineClient;

    @Override
    public String generateIoMessageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, boolean isIoUser) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, isIoUser);
        return templateEngineClient.ioMessageTemplate(language, informalCommunication);
    }

    @Override
    public String generatePecTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, boolean isIoUser) {
        LanguageEnum language = getLanguage(notificationRecipientInt.getAdditionalLanguages());
        InformalCommunication informalCommunication = mapToInformalCommunication(notificationInt, notificationRecipientInt, isIoUser);
        return templateEngineClient.pecTemplate(language, informalCommunication);
    }

    private LanguageEnum getLanguage(List<String> additionalLanguages) {
        return CollectionUtils.isEmpty(additionalLanguages)
                ? LanguageEnum.IT : LanguageEnum.fromValue(additionalLanguages.getFirst());
    }
}
