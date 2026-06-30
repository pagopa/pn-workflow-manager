package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateEngineClientImplTest {

    @Mock
    private TemplateApi templateApi;

    @InjectMocks
    private TemplateEngineClientImpl templateEngineClient;

    private LanguageEnum language;
    private InformalCommunication informalCommunication;

    @BeforeEach
    void setUp() {
        language = LanguageEnum.IT;
        informalCommunication = new InformalCommunication();
    }

    @Test
    void ioMessageTemplate_shouldReturnTemplate_whenTemplateApiRespondsSuccessfully() {
        // given
        String expectedTemplate = "<html>template content</html>";
        when(templateApi.informalIoCommunication(language, informalCommunication))
                .thenReturn(expectedTemplate);

        // when
        String result = templateEngineClient.ioMessageTemplate(language, informalCommunication);

        // then
        assertEquals(expectedTemplate, result);
        verify(templateApi).informalIoCommunication(language, informalCommunication);
        verifyNoMoreInteractions(templateApi);
    }

    @Test
    void ioMessageTemplate_shouldReturnNull_whenTemplateApiReturnsNull() {
        // given
        when(templateApi.informalIoCommunication(language, informalCommunication))
                .thenReturn(null);

        // when
        String result = templateEngineClient.ioMessageTemplate(language, informalCommunication);

        // then
        assertNull(result);
        verify(templateApi).informalIoCommunication(language, informalCommunication);
    }

    @Test
    void ioMessageTemplate_shouldPropagateException_whenTemplateApiThrows() {
        // given
        RuntimeException expectedException = new RuntimeException("external service error");
        when(templateApi.informalIoCommunication(language, informalCommunication))
                .thenThrow(expectedException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> templateEngineClient.ioMessageTemplate(language, informalCommunication));
        assertEquals("external service error", thrown.getMessage());

        verify(templateApi).informalIoCommunication(language, informalCommunication);
    }
}