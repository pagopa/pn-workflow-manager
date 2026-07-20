package it.pagopa.pn.workflowmanager.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PnWorkflowManagerConfigsTest {

    @Test
    void testConfigLoading() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("pn.workflow-manager.timeline-client-base-url", "http://localhost:8093")
                .withProperty("pn.workflow-manager.delivery-base-url", "http://localhost:8090")
                .withProperty("pn.workflow-manager.action-manager-base-url", "http://localhost:8092")
                .withProperty("pn.workflow-manager.topics.digital-queue", "pn-workflow-manager-digital-event-queue")
                .withProperty("pn.workflow-manager.topics.analog-queue", "pn-workflow-manager-analog-event-queue")
                .withProperty("pn.workflow-manager.topics.action-queue", "pn-workflow-manager-action-queue")
                .withProperty("pn.workflow-manager.topics.io-queue", "pn-workflow-manager-io-event-queue")
                .withProperty("pn.workflow-manager.pn-send-mode[0]", "2024-01-01T00:00:00Z;IGNORED;COVERPAGE|DOCUMENTS;DOCUMENTS;PAYMENTS")
                .withProperty("pn.workflow-manager.pn-send-mode[1]", "2024-06-01T00:00:00Z;IGNORED;PAYMENTS;COVERPAGE;COVERPAGE|PAYMENTS");

        PnWorkflowManagerConfigs pnNotificationCostServiceConfigs = Binder.get(environment)
                .bind("pn.workflow-manager", Bindable.of(PnWorkflowManagerConfigs.class))
                .orElseThrow(() -> new IllegalStateException("Failed to bind PnWorkflowManagerConfigs"));

        assertNotNull(pnNotificationCostServiceConfigs);


        PnWorkflowManagerConfigs.Topics topics =
                pnNotificationCostServiceConfigs.getTopics();
        assertNotNull(topics);
        Assertions.assertEquals("pn-workflow-manager-digital-event-queue", topics.getDigitalQueue());
        Assertions.assertEquals("pn-workflow-manager-analog-event-queue", topics.getAnalogQueue());
        Assertions.assertEquals("pn-workflow-manager-action-queue", topics.getActionQueue());
        Assertions.assertEquals("pn-workflow-manager-io-event-queue", topics.getIoQueue());
        Assertions.assertEquals(List.of(
                "2024-01-01T00:00:00Z;IGNORED;COVERPAGE|DOCUMENTS;DOCUMENTS;PAYMENTS",
                "2024-06-01T00:00:00Z;IGNORED;PAYMENTS;COVERPAGE;COVERPAGE|PAYMENTS"
        ), pnNotificationCostServiceConfigs.getPnSendMode());
    }

}