package it.pagopa.pn.workflowmanager.handler.actionspool;

import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.*;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionTypeTest {

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
                .channel(null)
                .build();

        Action action = Action.builder()
                .iun("IUN-999")
                .recipientIndex(2)
                .details(details)
                .build();

        String actionId = ActionType.TIMEOUT_WORKFLOW.buildActionId(action);
        assertEquals("IUN-999_timeout_workflow_recIndex_2_channel_null", actionId);
    }

    @Test
    void testDocumentCreationResponseBuildActionId() {
        DocumentCreationResponseActionDetails details = DocumentCreationResponseActionDetails.builder()
                .build();

        Action action = Action.builder()
                .iun("IUN-321")
                .recipientIndex(0)
                .details(details)
                .timelineId("timeline-123")
                .build();

        String actionId = ActionType.DOCUMENT_CREATION_RESPONSE.buildActionId(action);
        assertEquals("safe_storage_response_timelineId=timeline-123", actionId);
    }

    @Test
    void testSendCourtesyMessageActionBuildActionId() {
        SendCourtesyMessageActionDetails details = SendCourtesyMessageActionDetails.builder()
                .channel(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .retryIndex(0)
                .deliveryMode(null)
                .plannedChannels(null)
                .build();

        Action action = Action.builder()
                .iun("IUN-555")
                .recipientIndex(1)
                .details(details)
                .build();

        String actionId = ActionType.SEND_COURTESY_MESSAGE_ACTION.buildActionId(action);
        assertEquals("IUN-555_send_courtesy_message_recIndex_1_channel_EMAIL_retry_0", actionId);
    }

    @Test
    void testGetDetailsJavaClass() {
        assertEquals(NotHandledDetails.class, ActionType.POST_ACCEPTED_PROCESSING_COMPLETED.getDetailsJavaClass());
        assertEquals(NotHandledDetails.class, ActionType.END_WORKFLOW.getDetailsJavaClass());
        assertEquals(WorkflowDoneDetails.class, ActionType.WORKFLOW_DONE.getDetailsJavaClass());
        assertEquals(StartWorkflowDetails.class, ActionType.START_WORKFLOW.getDetailsJavaClass());
        assertEquals(TimeoutWorkflowDetails.class, ActionType.TIMEOUT_WORKFLOW.getDetailsJavaClass());
        assertEquals(DocumentCreationResponseActionDetails.class, ActionType.DOCUMENT_CREATION_RESPONSE.getDetailsJavaClass());
        assertEquals(SendCourtesyMessageActionDetails.class, ActionType.SEND_COURTESY_MESSAGE_ACTION.getDetailsJavaClass());
    }
}

