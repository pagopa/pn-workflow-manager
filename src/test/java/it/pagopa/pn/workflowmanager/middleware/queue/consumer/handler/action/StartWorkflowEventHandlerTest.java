package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.startworkflow.StartWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_REACHED;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartWorkflowEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private StartWorkflowActionHandler startWorkflowActionHandler;

    @Mock
    private MessageHeaders headers;

    private StartWorkflowEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final ChannelType TEST_CHANNEL = ChannelType.IO;
    private static final int TEST_STEP_INDEX = 1;

    @BeforeEach
    void setup() {
        handler = new StartWorkflowEventHandler(timelineUtils, startWorkflowActionHandler);
    }

    @Test
    void getSupportedEventType_shouldReturnStartWorkflow() {
        // Act
        SupportedEventType result = handler.getSupportedEventType();

        // Assert
        assertEquals(SupportedEventType.START_WORKFLOW, result);
    }

    @Test
    void getPayloadType_shouldReturnActionClass() {
        // Act
        Class<Action> result = handler.getPayloadType();

        // Assert
        assertEquals(Action.class, result);
    }

    @Test
    void handle_shouldExecuteStartWorkflowAction_whenWorkflowIsNotDone() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldNotExecuteStartWorkflowAction_whenWorkflowIsAlreadyDone() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);

        TimelineElementInternal workflowDoneElement = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(WORKFLOW_DONE_REACHED)
                .build();
        List<TimelineElementInternal> timelineElements = List.of(workflowDoneElement);

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        verify(startWorkflowActionHandler, never()).startWorkflowAction(anyString(), anyInt(), any(StartWorkflowDetails.class));
    }

    @Test
    void handle_shouldThrowException_whenStartWorkflowActionHandlerFails() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        RuntimeException expectedException = new RuntimeException("Test exception");

        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(false);
        doThrow(expectedException).when(startWorkflowActionHandler)
                .startWorkflowAction(anyString(), anyInt(), any(StartWorkflowDetails.class));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldCheckWorkflowDoneBeforeExecuting() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert - Verify order: check timeline before calling action handler
        var inOrder = inOrder(timelineUtils, startWorkflowActionHandler);
        inOrder.verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        inOrder.verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        inOrder.verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 3;
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(recIndex2)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();

        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(recIndex2),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(recIndex2),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, recIndex2, details);
    }

    @Test
    void handle_shouldCheckBothWorkflowDoneCategories() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);

        TimelineElementInternal element1 = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK)
                .build();
        List<TimelineElementInternal> timelineElements = List.of(element1);

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(
                anyList(),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED)
        );
    }

    @Test
    void handle_shouldNotThrowException_whenWorkflowIsAlreadyDone() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);

        TimelineElementInternal workflowDoneElement = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(WORKFLOW_DONE_UNREACHED)
                .build();
        List<TimelineElementInternal> timelineElements = List.of(workflowDoneElement);

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(startWorkflowActionHandler, never()).startWorkflowAction(anyString(), anyInt(), any());
    }

    private StartWorkflowDetails createStartWorkflowDetails() {
        return StartWorkflowDetails.builder()
                .channel(TEST_CHANNEL)
                .stepIdx(TEST_STEP_INDEX)
                .build();
    }

    private Action createAction(StartWorkflowDetails details) {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();
    }
}
