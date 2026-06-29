package it.pagopa.pn.workflowmanager.handler.actionspool;

import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionTypeTest {

    @Test
    void testBuildActionId() {
        Action action = Action.builder().iun("IUN-ABC").build();
        String actionId = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.buildActionId(action);
        assertEquals("IUN-ABC_post_accepted_processing", actionId);
    }

    @Test
    void testPostAcceptedProcessingCompletedBuildActionId() {
        Action action = Action.builder().iun("IUN-ABC").build();
        String actionId = ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.buildActionId(action);
        assertEquals("IUN-ABC_post_accepted_processing", actionId);
    }

    @Test
    void testEndWorkflowBuildActionId() {
        Action action = Action.builder()
                .iun("IUN-123")
                .recipientIndex(0)
                .build();
        String actionId = ActionType.END_WORKFLOW.buildActionId(action);
        assertEquals("IUN-123_end_workflow_recIndex_0", actionId);
    }

    @Test
    void testWorkflowDoneBuildActionId() {
        Action action = Action.builder()
                .iun("IUN-456")
                .recipientIndex(1)
                .build();
        String actionId = ActionType.WORKFLOW_DONE.buildActionId(action);
        assertEquals("IUN-456_workflow_done_recIndex_1", actionId);
    }

    @Test
    void testStartWorkflowBuildActionId() {
        StartWorkflowDetails details = StartWorkflowDetails.builder()
                .stepIdx(2)
                .channel(null)
                .build();

        Action action = Action.builder()
                .iun("IUN-789")
                .recipientIndex(0)
                .details(details)
                .build();

        String actionId = ActionType.START_WORKFLOW.buildActionId(action);
        assertEquals("IUN-789_start_workflow_recIndex_0_stepIndex_2_channel_null", actionId);
    }

    @Test
    void testTimeoutWorkflowBuildActionId() {
        TimeoutWorkflowDetails details = TimeoutWorkflowDetails.builder()
                .stepIdx(3)
                .channel(null)
                .build();

        Action action = Action.builder()
                .iun("IUN-999")
                .recipientIndex(2)
                .details(details)
                .build();

        String actionId = ActionType.TIMEOUT_WORKFLOW.buildActionId(action);
        assertEquals("IUN-999_timeout_workflow_recIndex_2_stepIndex_3_channel_null", actionId);
    }

    @Test
    void testGetDetailsJavaClass() {
        assertEquals(NotHandledDetails.class, ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.getDetailsJavaClass());
        assertEquals(NotHandledDetails.class, ActionType.END_WORKFLOW.getDetailsJavaClass());
        assertEquals(NotHandledDetails.class, ActionType.WORKFLOW_DONE.getDetailsJavaClass());
        assertEquals(StartWorkflowDetails.class, ActionType.START_WORKFLOW.getDetailsJavaClass());
        assertEquals(TimeoutWorkflowDetails.class, ActionType.TIMEOUT_WORKFLOW.getDetailsJavaClass());
    }

    @Test
    void testEnumValues() {
        ActionType[] values = ActionType.values();
        assertEquals(5, values.length);
        assertEquals(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED, values[0]);
        assertEquals(ActionType.END_WORKFLOW, values[1]);
        assertEquals(ActionType.WORKFLOW_DONE, values[2]);
        assertEquals(ActionType.START_WORKFLOW, values[3]);
        assertEquals(ActionType.TIMEOUT_WORKFLOW, values[4]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED,
                ActionType.valueOf("POST_ACCEPTED_PROCESSING_COMPLETED"));
        assertEquals(ActionType.END_WORKFLOW,
                ActionType.valueOf("END_WORKFLOW"));
        assertEquals(ActionType.WORKFLOW_DONE,
                ActionType.valueOf("WORKFLOW_DONE"));
        assertEquals(ActionType.START_WORKFLOW,
                ActionType.valueOf("START_WORKFLOW"));
        assertEquals(ActionType.TIMEOUT_WORKFLOW,
                ActionType.valueOf("TIMEOUT_WORKFLOW"));
    }
}

