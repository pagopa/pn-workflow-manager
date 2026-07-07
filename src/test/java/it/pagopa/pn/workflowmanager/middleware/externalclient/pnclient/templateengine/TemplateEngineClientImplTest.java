package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.templateengine;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.api.TemplateApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalCommunication;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.InformalEmailCommunicationSubject;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.templateengine.model.LanguageEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

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
    private InformalEmailCommunicationSubject informalEmailCommunicationSubject;

    @BeforeEach
    void setUp() {
        language = LanguageEnum.IT;
        informalCommunication = new InformalCommunication();
        informalEmailCommunicationSubject = new InformalEmailCommunicationSubject();
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

    @Test
    void pecBodyTemplate_shouldReturnTemplate_whenBodyTemplateApiRespondsSuccessfully() {
        // given
        String expectedTemplate = "<html>template content</html>";
        when(templateApi.informalPecCommunicationBody(language, informalCommunication))
                .thenReturn(expectedTemplate);

        // when
        String result = templateEngineClient.pecBodyTemplate(language, informalCommunication);

        // then
        assertEquals(expectedTemplate, result);
        verify(templateApi).informalPecCommunicationBody(language, informalCommunication);
        verifyNoMoreInteractions(templateApi);
    }

    @Test
    void pecBodyTemplate_shouldReturnNull_whenBodyTemplateApiReturnsNull() {
        // given
        when(templateApi.informalPecCommunicationBody(language, informalCommunication))
                .thenReturn(null);

        // when
        String result = templateEngineClient.pecBodyTemplate(language, informalCommunication);

        // then
        assertNull(result);
        verify(templateApi).informalPecCommunicationBody(language, informalCommunication);
    }

    @Test
    void pecBodyTemplate_shouldPropagateException_whenBodyTemplateApiThrows() {
        // given
        RuntimeException expectedException = new RuntimeException("external service error");
        when(templateApi.informalPecCommunicationBody(language, informalCommunication))
                .thenThrow(expectedException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> templateEngineClient.pecBodyTemplate(language, informalCommunication));
        assertEquals("external service error", thrown.getMessage());

        verify(templateApi).informalPecCommunicationBody(language, informalCommunication);
    }

    @Test
    void pecSubjectTemplate_shouldReturnTemplate_whenSubjectTemplateApiRespondsSuccessfully() {
        // given
        String expectedTemplate = "Subject content";
        when(templateApi.informalPecCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenReturn(expectedTemplate);

        // when
        String result = templateEngineClient.pecSubjectTemplate(language, informalEmailCommunicationSubject);

        // then
        assertEquals(expectedTemplate, result);
        verify(templateApi).informalPecCommunicationSubject(language, informalEmailCommunicationSubject);
        verifyNoMoreInteractions(templateApi);
    }

    @Test
    void pecSubjectTemplate_shouldReturnNull_whenSubjectTemplateApiReturnsNull() {
        // given
        when(templateApi.informalPecCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenReturn(null);

        // when
        String result = templateEngineClient.pecSubjectTemplate(language, informalEmailCommunicationSubject);

        // then
        assertNull(result);
        verify(templateApi).informalPecCommunicationSubject(language, informalEmailCommunicationSubject);
    }

    @Test
    void pecSubjectTemplate_shouldPropagateException_whenSubjectTemplateApiThrows() {
        // given
        RuntimeException expectedException = new RuntimeException("external service error");
        when(templateApi.informalPecCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenThrow(expectedException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> templateEngineClient.pecSubjectTemplate(language, informalEmailCommunicationSubject));
        assertEquals("external service error", thrown.getMessage());

        verify(templateApi).informalPecCommunicationSubject(language, informalEmailCommunicationSubject);
    }

    @Test
    void emailSubjectTemplate_shouldReturnTemplate_whenSubjectTemplateApiRespondsSuccessfully() {
        // given
        String expectedTemplate = "Email Subject content";
        when(templateApi.informalEmailCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenReturn(expectedTemplate);

        // when
        String result = templateEngineClient.emailSubjectTemplate(language, informalEmailCommunicationSubject);

        // then
        assertEquals(expectedTemplate, result);
        verify(templateApi).informalEmailCommunicationSubject(language, informalEmailCommunicationSubject);
        verifyNoMoreInteractions(templateApi);
    }

    @Test
    void emailSubjectTemplate_shouldReturnNull_whenSubjectTemplateApiReturnsNull() {
        // given
        when(templateApi.informalEmailCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenReturn(null);

        // when
        String result = templateEngineClient.emailSubjectTemplate(language, informalEmailCommunicationSubject);

        // then
        assertNull(result);
        verify(templateApi).informalEmailCommunicationSubject(language, informalEmailCommunicationSubject);
    }

    @Test
    void emailSubjectTemplate_shouldPropagateException_whenSubjectTemplateApiThrows() {
        // given
        RuntimeException expectedException = new RuntimeException("external service error");
        when(templateApi.informalEmailCommunicationSubject(language, informalEmailCommunicationSubject))
                .thenThrow(expectedException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> templateEngineClient.emailSubjectTemplate(language, informalEmailCommunicationSubject));
        assertEquals("external service error", thrown.getMessage());

        verify(templateApi).informalEmailCommunicationSubject(language, informalEmailCommunicationSubject);
    }

    @Test
    void coverpageTemplate_shouldReturnTemplate_whenCoverpageTemplateApiRespondsSuccessfully() {
        // given
        File expectedTemplate = mock(File.class);
        when(templateApi.informalAnalogCommunication(language, informalCommunication))
                .thenReturn(expectedTemplate);

        // when
        File result = templateEngineClient.coverpageTemplate(language, informalCommunication);

        // then
        assertEquals(expectedTemplate, result);
        verify(templateApi).informalAnalogCommunication(language, informalCommunication);
        verifyNoMoreInteractions(templateApi);
    }

    @Test
    void coverpageTemplate_shouldReturnNull_whenCoverpageTemplateApiReturnsNull() {
        // given
        when(templateApi.informalAnalogCommunication(language, informalCommunication))
                .thenReturn(null);

        // when
        File result = templateEngineClient.coverpageTemplate(language, informalCommunication);

        // then
        assertNull(result);
        verify(templateApi).informalAnalogCommunication(language, informalCommunication);
    }

    @Test
    void coverpageTemplate_shouldPropagateException_whenCoverpageTemplateApiThrows() {
        // given
        RuntimeException expectedException = new RuntimeException("external service error");
        when(templateApi.informalAnalogCommunication(language, informalCommunication))
                .thenThrow(expectedException);

        // when / then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> templateEngineClient.coverpageTemplate(language, informalCommunication));
        assertEquals("external service error", thrown.getMessage());

        verify(templateApi).informalAnalogCommunication(language, informalCommunication);
    }
}