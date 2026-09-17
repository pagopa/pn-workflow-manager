package it.pagopa.pn.workflowmanager;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PnWorkflowManagerApplication {
    public static void main(String[] args) {
        buildSpringApplicationWithListener().run(args);
    }

    static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(PnWorkflowManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }

}