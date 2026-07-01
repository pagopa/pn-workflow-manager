package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.endworkflow.EndWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_REACHED;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndWorkflowActionEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private EndWorkflowActionHandler endWorkflowActionHandler;

    @Mock
    private MessageHeaders headers;

    private EndWorkflowActionEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final String TEST_TIMELINE_ID = "TIMELINE-001";

    @BeforeEach
    void setup() {
        handler = new EndWorkflowActionEventHandler(timelineUtils, endWorkflowActionHandler);
    }

    @Test
    void getSupportedEventType_shouldReturnEndWorkflow() {
        // Act
        SupportedEventType result = handler.getSupportedEventType();

        // Assert
        assertEquals(SupportedEventType.END_WORKFLOW, result);
    }

    @Test
    void getPayloadType_shouldReturnActionClass() {
        // Act
        Class<Action> result = handler.getPayloadType();

        // Assert
        assertEquals(Action.class, result);
    }

    @Test
    void handle_shouldExecuteEndWorkflowAction_whenWorkflowIsNotDone() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldNotExecuteEndWorkflowAction_whenWorkflowIsAlreadyDone() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(endWorkflowActionHandler, never()).endWorkflowAction(anyString(), anyInt(), anyString());
    }

    @Test
    void handle_shouldPassCorrectIunToActionHandler() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(endWorkflowActionHandler).endWorkflowAction(eq(TEST_IUN), anyInt(), anyString());
    }

    @Test
    void handle_shouldPassCorrectRecipientIndexToActionHandler() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(endWorkflowActionHandler).endWorkflowAction(anyString(), eq(TEST_REC_INDEX), anyString());
    }

    @Test
    void handle_shouldPassCorrectTimelineIdToActionHandler() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(endWorkflowActionHandler).endWorkflowAction(anyString(), anyInt(), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldThrowException_whenEndWorkflowActionHandlerFails() {
        // Arrange
        Action action = createAction();
        RuntimeException expectedException = new RuntimeException("Test exception");

        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);
        doThrow(expectedException).when(endWorkflowActionHandler).endWorkflowAction(anyString(), anyInt(), anyString());

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldCheckWorkflowDoneBeforeExecuting() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert - Verify order: check timeline before calling action handler
        var inOrder = inOrder(timelineUtils, endWorkflowActionHandler);
        inOrder.verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        inOrder.verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(recIndex2)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .build();

        when(timelineUtils.checkTimelineCategories(TEST_IUN, recIndex2, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, recIndex2, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, recIndex2, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldHandleDifferentIuns() {
        // Arrange
        String differentIun = "DIFFERENT-IUN-999";
        Action action = Action.builder()
                .iun(differentIun)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .build();

        when(timelineUtils.checkTimelineCategories(differentIun, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(differentIun, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(endWorkflowActionHandler).endWorkflowAction(differentIun, TEST_REC_INDEX, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldCheckBothWorkflowDoneCategories() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(
                eq(TEST_IUN),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED)
        );
    }

    @Test
    void handle_shouldNotThrowException_whenWorkflowIsAlreadyDone() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, headers));
    }

    @Test
    void handle_shouldHandleNullHeaders() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);
    }

    @Test
    void handle_shouldHandleActionWithDifferentTimelineId() {
        // Arrange
        String customTimelineId = "CUSTOM-TIMELINE-999";
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .timelineId(customTimelineId)
                .build();

        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(endWorkflowActionHandler).endWorkflowAction(TEST_IUN, TEST_REC_INDEX, customTimelineId);
    }

    private Action createAction() {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .build();
    }
}