package it.pagopa.pn.workflowmanager.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PnWorkflowManagerConfigsTest {

    @Test
    void testTopicsGetterSetter() {
        PnWorkflowManagerConfigs.Topics topics = new PnWorkflowManagerConfigs.Topics();
        topics.setPnWorkflowManagerActionQueue("test-queue");
        assertEquals("test-queue", topics.getPnWorkflowManagerActionQueue());
    }

    @Test
    void testEventBusGetterSetter() {
        PnWorkflowManagerConfigs.EventBus eventBus = new PnWorkflowManagerConfigs.EventBus();
        eventBus.setName("my-bus");
        eventBus.setSource("my-source");
        eventBus.setOutcomeEventDetailType("my-detail-type");

        assertEquals("my-bus", eventBus.getName());
        assertEquals("my-source", eventBus.getSource());
        assertEquals("my-detail-type", eventBus.getOutcomeEventDetailType());
    }

    @Test
    void testConfigsGetterSetter() {
        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();

        PnWorkflowManagerConfigs.Topics topics = new PnWorkflowManagerConfigs.Topics();
        topics.setPnWorkflowManagerActionQueue("queue-name");
        configs.setTopics(topics);

        PnWorkflowManagerConfigs.EventBus eventBus = new PnWorkflowManagerConfigs.EventBus();
        eventBus.setName("bus-name");
        configs.setEventBus(eventBus);

        assertNotNull(configs.getTopics());
        assertEquals("queue-name", configs.getTopics().getPnWorkflowManagerActionQueue());
        assertNotNull(configs.getEventBus());
        assertEquals("bus-name", configs.getEventBus().getName());
    }

    @Test
    void testConfigsInit() {
        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        // init() esegue solo un log.info, verifichiamo che non lanci eccezioni
        assertDoesNotThrow(configs::init);
    }
}
