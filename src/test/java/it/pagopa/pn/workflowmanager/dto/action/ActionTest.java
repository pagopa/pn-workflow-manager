package it.pagopa.pn.workflowmanager.dto.action;

import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    @Test
    void testActionBuilder() {
        Instant now = Instant.now();
        Action action = Action.builder()
                .iun("IUN-TEST-001")
                .actionId("action-123")
                .notBefore(now)
                .type(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED)
                .recipientIndex(0)
                .timelineId("timeline-001")
                .timeslot("2026-01-01T00:00:00Z")
                .build();

        assertEquals("IUN-TEST-001", action.getIun());
        assertEquals("action-123", action.getActionId());
        assertEquals(now, action.getNotBefore());
        assertEquals(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED, action.getType());
        assertEquals(0, action.getRecipientIndex());
        assertEquals("timeline-001", action.getTimelineId());
        assertEquals("2026-01-01T00:00:00Z", action.getTimeslot());
    }

    @Test
    void testPostAcceptedProcessingCompletedBuildActionId() {
        Action action = Action.builder().iun("IUN-ABC").build();
        String actionId = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.buildActionId(action);
        assertEquals("IUN-ABC_post_accepted_processing", actionId);
    }

    @Test
    void testActionNoArgsConstructor() {
        Action action = new Action();
        assertNull(action.getIun());
        assertNull(action.getType());
    }

    @Test
    void testActionEqualsAndHashCode() {
        Instant now = Instant.now();
        Action a1 = Action.builder().iun("IUN-001").notBefore(now).build();
        Action a2 = Action.builder().iun("IUN-001").notBefore(now).build();
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void testActionToString() {
        Action action = Action.builder().iun("IUN-001").build();
        assertNotNull(action.toString());
        assertTrue(action.toString().contains("IUN-001"));
    }

    @Test
    void testActionToBuilder() {
        Action original = Action.builder().iun("IUN-001").actionId("id-1").build();
        Action copy = original.toBuilder().actionId("id-2").build();
        assertEquals("IUN-001", copy.getIun());
        assertEquals("id-2", copy.getActionId());
    }

    @Test
    void testAllArgsConstructor() {
        Instant now = Instant.now();
        Action action = new Action("IUN-002", "act-2", now, ActionType.POST_ACCEPTED_PROCESSING_COMPLETED, 1, "tl-1", "slot", CommunicationType.INFORMAL,null);
        assertEquals("IUN-002", action.getIun());
        assertEquals(1, action.getRecipientIndex());
    }
}

