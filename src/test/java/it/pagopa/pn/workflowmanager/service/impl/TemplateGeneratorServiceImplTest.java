package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine.TemplateEngineClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

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
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.ioMessageTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generateIoMessageTemplate(notificationInt, notificationRecipientInt, true);

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
    void shouldGeneratePecTemplate(
            List<String> additionalLanguages,
            LanguageEnum expectedLanguage
    ) {
        NotificationInt notificationInt = buildNotification();
        NotificationRecipientInt notificationRecipientInt = buildNotificationRecipient(additionalLanguages);
        String expectedMessageTemplate = "template-content";

        when(templateEngineClient.pecTemplate(Mockito.eq(expectedLanguage), Mockito.any())).thenReturn(expectedMessageTemplate);

        String result = templateGeneratorService.generatePecTemplate(notificationInt, notificationRecipientInt, true);

        assertEquals(expectedMessageTemplate, result);
        verify(templateEngineClient).pecTemplate(Mockito.eq(expectedLanguage), Mockito.any());
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
}