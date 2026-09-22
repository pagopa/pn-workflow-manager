package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.action.sendchannelmessage.SendChannelMessageActionHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendChannelMessageEventHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private SendChannelMessageActionHandler sendChannelMessageActionHandler;

    @Mock
    private MessageHeaders headers;

    private SendChannelMessageEventHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_ACTION_ID = "ACTION-001";
    private static final ChannelType TEST_CHANNEL = ChannelType.IO;

    @BeforeEach
    void setup() {
        handler = new SendChannelMessageEventHandler(timelineUtils, sendChannelMessageActionHandler);
    }

    @Test
    void getSupportedEventType_shouldReturnSendChannelMessage() {
        assertEquals(SupportedEventType.SEND_CHANNEL_MESSAGE, handler.getSupportedEventType());
    }

    @Test
    void getPayloadType_shouldReturnActionClass() {
        assertEquals(Action.class, handler.getPayloadType());
    }

    @Test
    void handle_shouldExecuteAction_whenWorkflowIsNotCompleted() {
        SendChannelMessageDetails details = createDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = List.of();

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

        assertDoesNotThrow(() -> handler.handle(action, headers));

        verify(sendChannelMessageActionHandler).sendChannelMessageAction(TEST_IUN, TEST_REC_INDEX, details);
    }

    @Test
    void handle_shouldNotExecuteAction_whenWorkflowIsAlreadyCompleted() {
        SendChannelMessageDetails details = createDetails();
        Action action = createAction(details);
        List<TimelineElementInternal> timelineElements = List.of(TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(WORKFLOW_DONE_REACHED)
                .build());

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

        assertDoesNotThrow(() -> handler.handle(action, headers));

        verify(sendChannelMessageActionHandler, never()).sendChannelMessageAction(anyString(), anyInt(), any(SendChannelMessageDetails.class));
    }

    @Test
    void handle_shouldThrowException_whenActionHandlerFails() {
        SendChannelMessageDetails details = createDetails();
        Action action = createAction(details);
        RuntimeException expectedException = new RuntimeException("Test exception");

        when(timelineUtils.getTimelineElementInternals(anyString())).thenReturn(List.<TimelineElementInternal>of().stream());
        when(timelineUtils.checkTimelineCategories(
                anyList(),
                eq(TEST_REC_INDEX),
                eq(WORKFLOW_DONE_REACHED),
                eq(WORKFLOW_DONE_UNREACHED),
                eq(WORKFLOW_ENDED_REACHED),
                eq(WORKFLOW_ENDED_UNREACHED),
                eq(WORKFLOW_ENDED_UNDELIVERABLE)
        )).thenReturn(false);
        doThrow(expectedException).when(sendChannelMessageActionHandler)
                .sendChannelMessageAction(anyString(), anyInt(), any(SendChannelMessageDetails.class));

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> handler.handle(action, headers));

        assertEquals("Test exception", thrownException.getMessage());
    }

    private SendChannelMessageDetails createDetails() {
        return SendChannelMessageDetails.builder()
                .channel(TEST_CHANNEL)
                .addressSource(DigitalAddressSourceInt.PLATFORM)
                .build();
    }

    private Action createAction(SendChannelMessageDetails details) {
        return Action.builder()
                .iun(TEST_IUN)
                .recipientIndex(TEST_REC_INDEX)
                .actionId(TEST_ACTION_ID)
                .details(details)
                .build();
    }
}
