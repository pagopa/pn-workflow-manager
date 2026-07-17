package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.documentcreation.DocumentCreationResponseHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentCreationResponseEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private DocumentCreationResponseHandler documentCreationResponseHandler;

    @Mock
    private MessageHeaders headers;

    private DocumentCreationResponseEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final DocumentCreationResponseActionDetails TEST_ACTION_DETAILS = DocumentCreationResponseActionDetails.builder()
            .key("test-key")
            .documentCreationType("test-type")
            .timelineElementId("test-element-id")
            .build();

    @BeforeEach
    void setup() {
        handler = new DocumentCreationResponseEventHandler(timelineUtils, documentCreationResponseHandler);
    }

    @Test
    void handle_shouldExecuteEndWorkflowAction_whenWorkflowIsNotCompleted() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(
                anyList(),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED),
                eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED),
                eq(WORKFLOW_ENDED_UNDELIVERABLE)
        )).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED), eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED), eq(WORKFLOW_ENDED_UNDELIVERABLE));
        verify(documentCreationResponseHandler).handleResponseReceived(TEST_IUN, TEST_REC_INDEX, TEST_ACTION_DETAILS);
    }

    @Test
    void handle_shouldNotExecuteEndWorkflowAction_whenWorkflowIsAlreadyCompleted() {
        // Arrange
        Action action = createAction();
        List<TimelineElementInternal> timelineElements = createTimelineWithWorkflowDoneReached();

        when(timelineUtils.getTimelineElementInternals(TEST_IUN)).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(
                anyList(),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED),
                eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED),
                eq(WORKFLOW_ENDED_UNDELIVERABLE)
        )).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).getTimelineElementInternals(TEST_IUN);
        verify(timelineUtils).checkTimelineCategories(anyList(), eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED), eq(WORKFLOW_DONE_UNREACHED), eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED), eq(WORKFLOW_ENDED_UNDELIVERABLE));
        verify(documentCreationResponseHandler, never()).handleResponseReceived(any(), any(), any());
    }

    @Test
    void handle_shouldThrowException_whenDocumentCreationResponseActionHandlerFails() {
        // Arrange
        Action action = createAction();
        RuntimeException expectedException = new RuntimeException("Test exception");

        List<TimelineElementInternal> timelineElements = createTimelineWithoutWorkflowDone();

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(timelineElements.stream());
        when(timelineUtils.checkTimelineCategories(
                anyList(),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED),
                eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED),
                eq(WORKFLOW_ENDED_UNDELIVERABLE)
        )).thenReturn(false);
        doThrow(expectedException).when(documentCreationResponseHandler).handleResponseReceived(TEST_IUN, TEST_REC_INDEX, TEST_ACTION_DETAILS);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
        verify(documentCreationResponseHandler).handleResponseReceived(TEST_IUN, TEST_REC_INDEX, TEST_ACTION_DETAILS);
    }

    private Action createAction() {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(TEST_ACTION_DETAILS)
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
}