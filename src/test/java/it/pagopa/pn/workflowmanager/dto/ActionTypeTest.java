package it.pagopa.pn.workflowmanager.dto;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.ActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTypeTest {

    @Test
    void testBuildActionId() {
        Action action = Action.builder().iun("IUN-ABC").build();
        String actionId = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.buildActionId(action);
        assertEquals("IUN-ABC_post_accepted_processing", actionId);
    }

    @Test
    void testGetDetailsJavaClass() {
        assertNotNull(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.getDetailsJavaClass());
    }

    @Test
    void testEnumValues() {
        ActionType[] values = ActionType.values();
        assertEquals(1, values.length);
        assertEquals(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED, values[0]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED,
                ActionType.valueOf("POST_ACCEPTED_PROCESSING_COMPLETED"));
    }
}

