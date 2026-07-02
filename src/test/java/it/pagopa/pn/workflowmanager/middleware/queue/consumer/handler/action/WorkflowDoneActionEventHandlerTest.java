package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.doneworkflow.WorkflowDoneActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowDoneActionEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private WorkflowDoneActionHandler workflowDoneActionHandler;

    @Mock
    private MessageHeaders headers;

    private WorkflowDoneActionEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final String TEST_TIMELINE_ID = "TIMELINE-001";

    @BeforeEach
    void setup() {
        handler = new WorkflowDoneActionEventHandler(timelineUtils, workflowDoneActionHandler);
    }

    @Test
    void getSupportedEventType_shouldReturnWorkflowDone() {
        // Act
        SupportedEventType result = handler.getSupportedEventType();

        // Assert
        assertEquals(SupportedEventType.WORKFLOW_DONE, result);
    }

    @Test
    void getPayloadType_shouldReturnActionClass() {
        // Act
        Class<Action> result = handler.getPayloadType();

        // Assert
        assertEquals(Action.class, result);
    }

    @Test
    void handle_shouldExecuteWorkflowDoneAction_whenWorkflowIsNotDone() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any()))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any());
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldNotExecuteWorkflowDoneAction_whenWorkflowIsAlreadyDone() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any()))
                .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any());
        verify(workflowDoneActionHandler, never()).doneWorkflowAction(anyList(), anyString(), anyInt(), anyString());
    }

    @Test
    void handle_shouldThrowException_whenWorkflowDoneActionHandlerFails() {
        // Arrange
        Action action = createAction();
        RuntimeException expectedException = new RuntimeException("Test exception");

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(false);
        doThrow(expectedException).when(workflowDoneActionHandler).doneWorkflowAction(anyList(), anyString(), anyInt(), anyString());

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldCheckWorkflowDoneBeforeExecuting() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any()))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert - Verify order: check timeline before calling action handler
        var inOrder = inOrder(timelineUtils, workflowDoneActionHandler);
        inOrder.verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        inOrder.verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any());
        inOrder.verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 3;
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(recIndex2)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .build();

        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(recIndex2), any(), any()))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(recIndex2), any(), any());
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(recIndex2), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldNotThrowException_whenWorkflowIsAlreadyDone() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, headers));
        verify(workflowDoneActionHandler, never()).doneWorkflowAction(anyList(), anyString(), anyInt(), anyString());
    }

    @Test
    void handle_shouldHandleNullHeaders() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldPropagateException_whenActionHandlerThrowsCheckedException() {
        // Arrange
        Action action = createAction();
        IllegalStateException checkedException = new IllegalStateException("Checked exception");

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(false);
        doThrow(checkedException).when(workflowDoneActionHandler)
                .doneWorkflowAction(anyList(), anyString(), anyInt(), anyString());

        // Act & Assert
        IllegalStateException thrownException = assertThrows(IllegalStateException.class,
                () -> handler.handle(action, headers));

        assertEquals("Checked exception", thrownException.getMessage());
    }

    @Test
    void handle_shouldExecuteSuccessfully_withCompleteActionObject() {
        // Arrange
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .build();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX), any(), any());
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
    }

    @Test
    void handle_shouldCallActionHandlerExactlyOnce_whenWorkflowIsNotDone() {
        // Arrange
        Action action = createAction();
        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(workflowDoneActionHandler, times(1)).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID));
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