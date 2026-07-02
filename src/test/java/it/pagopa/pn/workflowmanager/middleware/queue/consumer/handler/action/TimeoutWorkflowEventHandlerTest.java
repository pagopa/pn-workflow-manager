package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.timeoutworkflow.TimeoutWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
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
class TimeoutWorkflowEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private TimeoutWorkflowActionHandler timeoutWorkflowActionHandler;

    @Mock
    private MessageHeaders headers;

    private TimeoutWorkflowEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final ChannelType TEST_CHANNEL = ChannelType.IO;

    @BeforeEach
    void setup() {
        handler = new TimeoutWorkflowEventHandler(timelineUtils, timeoutWorkflowActionHandler);
    }

    @Test
    void getSupportedEventType_shouldReturnTimeoutWorkflow() {
        // Act
        SupportedEventType result = handler.getSupportedEventType();

        // Assert
        assertEquals(SupportedEventType.TIMEOUT_WORKFLOW, result);
    }

    @Test
    void getPayloadType_shouldReturnActionClass() {
        // Act
        Class<Action> result = handler.getPayloadType();

        // Assert
        assertEquals(Action.class, result);
    }

    @Test
    void handle_shouldExecuteTimeoutWorkflowAction_whenWorkflowIsNotDone() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

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
        verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldNotExecuteTimeoutWorkflowAction_whenWorkflowIsAlreadyDone() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = createTimelineWithWorkflowDoneReached();

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
        verify(timeoutWorkflowActionHandler, never()).timeoutWorkflowAction(anyString(), anyInt(), any(TimeoutWorkflowDetails.class));
    }

    @Test
    void handle_shouldThrowException_whenTimeoutWorkflowActionHandlerFails() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        RuntimeException expectedException = new RuntimeException("Test exception");

        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(false);
        doThrow(expectedException).when(timeoutWorkflowActionHandler)
                .timeoutWorkflowAction(anyString(), anyInt(), any(TimeoutWorkflowDetails.class));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldCheckWorkflowDoneBeforeExecuting() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED)))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert - Verify order: check timeline before calling action handler
        var inOrder = inOrder(timelineUtils, timeoutWorkflowActionHandler);
        inOrder.verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        inOrder.verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        inOrder.verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(recIndex2)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();

        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

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
        verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, recIndex2, details);
    }

    @Test
    void handle_shouldNotThrowException_whenWorkflowIsAlreadyDone() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = createTimelineWithWorkflowDoneUnreached();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, headers));
        verify(timeoutWorkflowActionHandler, never()).timeoutWorkflowAction(anyString(), anyInt(), any());
    }

    @Test
    void handle_shouldHandleNullHeaders() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldPropagateException_whenActionHandlerThrowsCheckedException() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        Action action = createAction(details);
        RuntimeException checkedException = new IllegalStateException("Checked exception");

        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(false);
        doThrow(checkedException).when(timeoutWorkflowActionHandler)
                .timeoutWorkflowAction(anyString(), anyInt(), any(TimeoutWorkflowDetails.class));

        // Act & Assert
        IllegalStateException thrownException = assertThrows(IllegalStateException.class,
                () -> handler.handle(action, headers));

        assertEquals("Checked exception", thrownException.getMessage());
    }

    @Test
    void handle_shouldExecuteSuccessfully_withCompleteActionObject() {
        // Arrange
        TimeoutWorkflowDetails details = new TimeoutWorkflowDetails();
        details.setChannel(ChannelType.IO);

        Action action = Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();

        List<TimelineElementInternal> timelineElements = createTimelineWithWorkflowDoneUnreached();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(anyList(), anyInt(),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED))).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED));
        verify(timeoutWorkflowActionHandler).timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    private TimeoutWorkflowDetails createTimeoutWorkflowDetails() {
        TimeoutWorkflowDetails details = new TimeoutWorkflowDetails();
        details.setChannel(TEST_CHANNEL);
        return details;
    }

    private Action createAction(TimeoutWorkflowDetails details) {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();
    }

    private List<TimelineElementInternal> createTimelineWithoutWorkflowDone() {
        TimelineElementInternal element = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK)
                .build();
        return List.of(element);
    }

    private List<TimelineElementInternal> createTimelineWithWorkflowDoneReached() {
        TimelineElementInternal element = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(WORKFLOW_DONE_REACHED)
                .build();
        return List.of(element);
    }

    private List<TimelineElementInternal> createTimelineWithWorkflowDoneUnreached() {
        TimelineElementInternal element = TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(WORKFLOW_DONE_UNREACHED)
                .build();
        return List.of(element);
    }

}