package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.doneworkflow.WorkflowDoneActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.WorkflowDoneDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
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
    private static final WorkflowDoneDetails TEST_WORKFLOW_DONE_DETAILS = WorkflowDoneDetails.builder()
            .completionFeedback(DesiredFeedbackType.SENT)
            .build();

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
    void handle_shouldExecuteWorkflowDoneAction_whenWorkflowIsNotCompleted() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID), eq(TEST_WORKFLOW_DONE_DETAILS));
    }

    @Test
    void handle_shouldThrowException_whenWorkflowDoneActionHandlerFails() {
        // Arrange
        Action action = createAction();
        RuntimeException expectedException = new RuntimeException("Test exception");

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(Stream.empty());
        doThrow(expectedException).when(workflowDoneActionHandler).doneWorkflowAction(anyList(), anyString(), anyInt(), anyString(), any(WorkflowDoneDetails.class));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(workflowDoneActionHandler).doneWorkflowAction(anyList(), eq(TEST_IUN), eq(TEST_REC_INDEX), eq(TEST_TIMELINE_ID), eq(TEST_WORKFLOW_DONE_DETAILS));
    }

    private Action createAction() {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .timelineId(TEST_TIMELINE_ID)
                .details(TEST_WORKFLOW_DONE_DETAILS)
                .build();
    }
}