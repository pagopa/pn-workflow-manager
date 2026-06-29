package it.pagopa.pn.workflowmanager.handler.router.deserializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.workflowmanager.exceptions.PnEventRouterException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.router.deserializer.impl.JsonRouterDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

import java.util.Map;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_ROUTER_DESERIALIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonRouterDeserializerTest {

    private final JsonRouterDeserializer deserializer = new JsonRouterDeserializer(new ObjectMapper());

    @Test
    void deserializeThrowsExceptionWhenPayloadIsNotString() {
        Message<Integer> message = mock(Message.class);
        when(message.getPayload()).thenReturn(123);

        PnEventRouterException exception = assertThrows(PnEventRouterException.class,
                () -> deserializer.deserialize(message, Object.class));
        assertEquals(ERROR_CODE_DELIVERYPUSH_ROUTER_DESERIALIZATION, exception.getProblem().getErrors().getFirst().getCode());
    }

    @Test
    void deserializeThrowsExceptionWhenJsonProcessingFails() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("{invalidJson}");

        PnEventRouterException exception = assertThrows(PnEventRouterException.class,
                () -> deserializer.deserialize(message, Object.class));
        assertEquals(ERROR_CODE_DELIVERYPUSH_ROUTER_DESERIALIZATION, exception.getProblem().getErrors().getFirst().getCode());
    }

    @Test
    void deserializeReturnsObjectWhenPayloadIsValidJson() {
        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("{\"key\":\"value\"}");

        Map result = deserializer.deserialize(message, Map.class);
        assertEquals("value", result.get("key"));
    }
}