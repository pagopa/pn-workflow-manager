package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.startworkflow.StartWorkflowActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

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
        
        // checkTimelineCategories returns false when WORKFLOW_DONE categories don't exist (workflow not done yet)
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldNotExecuteStartWorkflowAction_whenWorkflowIsAlreadyDone() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        // checkTimelineCategories returns true when WORKFLOW_DONE categories exist (workflow is already done)
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(true);

        // Act
        assertDoesNotThrow(() -> handler.handle(action, headers));

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(startWorkflowActionHandler, never()).startWorkflowAction(anyString(), anyInt(), any(StartWorkflowDetails.class));
    }

    @Test
    void handle_shouldPassCorrectIunToActionHandler() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(eq(TEST_IUN), anyInt(), any(StartWorkflowDetails.class));
    }

    @Test
    void handle_shouldPassCorrectRecipientIndexToActionHandler() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(anyString(), eq(TEST_REC_INDEX), any(StartWorkflowDetails.class));
    }

    @Test
    void handle_shouldPassCorrectDetailsToActionHandler() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(anyString(), anyInt(), eq(details));
    }

    @Test
    void handle_shouldCastDetailsToStartWorkflowDetails() {
        // Arrange
        StartWorkflowDetails details = StartWorkflowDetails.builder()
                .channel(ChannelType.IO)
                .stepIdx(2)
                .build();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
        assertEquals(ChannelType.IO, details.getChannel());
        assertEquals(2, details.getStepIdx());
    }

    @Test
    void handle_shouldThrowException_whenStartWorkflowActionHandlerFails() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        RuntimeException expectedException = new RuntimeException("Test exception");
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);
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
        
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert - Verify order: check timeline before calling action handler
        var inOrder = inOrder(timelineUtils, startWorkflowActionHandler);
        inOrder.verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
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
        
        when(timelineUtils.checkTimelineCategories(TEST_IUN, recIndex2, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, recIndex2, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, recIndex2, details);
    }

    @Test
    void handle_shouldHandleDifferentIuns() {
        // Arrange
        String differentIun = "DIFFERENT-IUN-999";
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = Action.builder()
                .iun(differentIun)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();
        
        when(timelineUtils.checkTimelineCategories(differentIun, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED))
                .thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(timelineUtils).checkTimelineCategories(differentIun, TEST_REC_INDEX, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
        verify(startWorkflowActionHandler).startWorkflowAction(differentIun, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldCheckBothWorkflowDoneCategories() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
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
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(startWorkflowActionHandler, never()).startWorkflowAction(anyString(), anyInt(), any());
    }

    @Test
    void handle_shouldHandleNullHeaders() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> handler.handle(action, null));
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldHandleDifferentChannels() {
        // Arrange
        StartWorkflowDetails details = StartWorkflowDetails.builder()
                .channel(ChannelType.IO)
                .stepIdx(TEST_STEP_INDEX)
                .build();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
        assertEquals(ChannelType.IO, details.getChannel());
    }

    @Test
    void handle_shouldHandleDifferentStepIndices() {
        // Arrange
        int customStepIndex = 5;
        StartWorkflowDetails details = StartWorkflowDetails.builder()
                .channel(TEST_CHANNEL)
                .stepIdx(customStepIndex)
                .build();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);
        assertEquals(customStepIndex, details.getStepIdx());
    }

    @Test
    void handle_shouldPassDetailsObjectAsIs_withoutModification() {
        // Arrange
        StartWorkflowDetails details = StartWorkflowDetails.builder()
                .channel(ChannelType.IO)
                .stepIdx(99)
                .build();
        Action action = createAction(details);
        
        when(timelineUtils.checkTimelineCategories(anyString(), anyInt(), any(), any())).thenReturn(false);

        // Act
        handler.handle(action, headers);

        // Assert
        verify(startWorkflowActionHandler).startWorkflowAction(eq(TEST_IUN), eq(TEST_REC_INDEX), argThat(d -> 
                d.getChannel().equals(ChannelType.IO) && d.getStepIdx() == 99
        ));
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
