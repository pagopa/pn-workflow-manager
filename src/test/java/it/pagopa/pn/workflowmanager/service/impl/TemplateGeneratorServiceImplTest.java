package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine.TemplateEngineClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TemplateGeneratorServiceImplTest {
    private TemplateEngineClient templateEngineClient;

    private TemplateGeneratorServiceImpl templateGeneratorService;

    @BeforeEach
    void setUp() {
        templateEngineClient = mock(TemplateEngineClient.class);
        templateGeneratorService = new TemplateGeneratorServiceImpl(templateEngineClient);
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGenerateIoMessageTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        Campaign campaign = buildCampaign();
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.ioMessageTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generateIoMessageTemplate(notificationInt, notificationRecipientInt, campaign);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).ioMessageTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    private static Stream<Arguments> provideAdditionalLanguageArguments() {
        return Stream.of(
                Arguments.of(List.of(),  LanguageEnum.IT),
                Arguments.of(List.of("EN", "DE"), LanguageEnum.EN),
                Arguments.of(null, LanguageEnum.IT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGeneratePecBodyTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        Campaign campaign = buildCampaign();
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.pecBodyTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generatePecBodyTemplate(notificationInt, notificationRecipientInt, campaign);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).pecBodyTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGeneratePecSubjectTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.pecSubjectTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generatePecSubjectTemplate(notificationInt, notificationRecipientInt);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).pecSubjectTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGenerateEmailBodyTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        Campaign campaign = buildCampaign();
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.emailBodyTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generateEmailBodyTemplate(notificationInt, notificationRecipientInt, campaign);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).emailBodyTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGenerateEmailSubjectTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.emailSubjectTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generateEmailSubjectTemplate(notificationInt, notificationRecipientInt);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).emailSubjectTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGenerateAnalogTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        Campaign campaign = buildCampaign();
        File expectedCoverpageTemplate = mock(File.class);

        when(templateEngineClient.coverpageTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedCoverpageTemplate);

        File result = templateGeneratorService.generateCoverpageTemplate(notificationInt, notificationRecipientInt, campaign);

        assertEquals(expectedCoverpageTemplate, result);
        verify(templateEngineClient).coverpageTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("provideAdditionalLanguageArguments")
    void shouldGenerateSmsTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.smsTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generateSmsTemplate(notificationInt, notificationRecipientInt);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).smsTemplate(Mockito.eq(expectedLanguage), Mockito.any());
    }

    private NotificationInt buildNotification() {
        return NotificationInt.builder()
                .iun("iun")
                .sender(NotificationSenderInt.builder()
                        .paTaxId("senderTaxId")
                        .paDenomination("senderDenomination")
                        .paId("senderId")
                        .build())
                .documents(Collections.emptyList())
                .build();
    }

    private NotificationRecipientInt buildNotificationRecipient(List<String> additionalLanguages) {
        return NotificationRecipientInt.builder()
                .taxId("recipientTaxId")
                .denomination("recipientDenomination")
                .recipientType(RecipientTypeInt.PF)
                .additionalLanguages(additionalLanguages)
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .language("IT")
                                .longBody("longBody")
                                .build())
                        .build())
                .build();
    }

    private Campaign buildCampaign() {
        return Campaign.builder()
                .campaignId("campaignId")
                .senderId("senderId")
                .title("title")
                .descriptionScope("descriptionScope")
                .status(CampaignStatus.IN_PROGRESS)
                .serviceId("serviceId")
                .serviceName("serviceName")
                .workflow(List.of())
                .build();
    }
}