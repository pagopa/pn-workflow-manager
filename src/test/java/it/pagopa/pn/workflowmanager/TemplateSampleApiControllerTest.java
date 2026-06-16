package it.pagopa.pn.workflowmanager;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TemplateSampleApiControllerTest {

    @Test
    void testBuildSpringApplicationWithListener() {
        SpringApplication app = PnWorkflowManagerApplication.buildSpringApplicationWithListener();
        assertNotNull(app, "SpringApplication non deve essere null");

        boolean listenerFound = app.getListeners().stream()
                .anyMatch(TaskIdApplicationListener.class::isInstance);
        assertTrue(listenerFound, "TaskIdApplicationListener deve essere aggiunto alla SpringApplication");
    }
}