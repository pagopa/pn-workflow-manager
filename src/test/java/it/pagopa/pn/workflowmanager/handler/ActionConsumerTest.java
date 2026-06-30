package it.pagopa.pn.workflowmanager.handler;

import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.ActionConsumer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.EventRouter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
class ActionConsumerTest {

    @InjectMocks
    private ActionConsumer actionConsumer;
    @Mock
    private EventRouter eventRouter;

    @Test
    void testWorkflowManagerActionConsumer_success() {
        Action action = Action.builder()
                .iun("IUN-TEST")
                .actionId("act-001")
                .type(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED)
                .build();

        Message<Action> message = MessageBuilder.withPayload(action)
                .setHeader("aws_messageId", "msg-001")
                .setHeader("X-Amzn-Trace-Id", "trace-001")
                .setHeader("iun", "IUN-TEST")
                .build();

        assertDoesNotThrow(() -> actionConsumer.workflowManagerActionConsumer(message));
    }

    @Test
    void testWorkflowManagerActionConsumer_noHeaders() {
        Action action = Action.builder()
                .iun("IUN-002")
                .type(ActionType.POST_ACCEPTED_PROCESSING_COMPLETED)
                .build();

        Message<Action> message = MessageBuilder.withPayload(action).build();

        assertDoesNotThrow(() -> actionConsumer.workflowManagerActionConsumer(message));
    }
}

