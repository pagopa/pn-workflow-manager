package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.Action;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.SupportedEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PostAcceptedProcessingEventHandlerTest {
    @Mock
    private MessageHeaders headers;
    @InjectMocks
    private PostAcceptedProcessingEventHandler handler;

    @Test
    void getSupportedEventTypeReturnsCorrectType() {
        assertEquals(SupportedEventType.POST_ACCEPTED_PROCESSING_COMPLETED, handler.getSupportedEventType());
    }

    @Test
    void handleExecutes() {
        Action action = Action.builder()
                .iun("iun_123")
                .recipientIndex(0)
                .build();

        Assertions.assertDoesNotThrow(() -> handler.handle(action, headers));
    }
}