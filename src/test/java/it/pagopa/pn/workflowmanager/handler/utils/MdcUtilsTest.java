package it.pagopa.pn.workflowmanager.handler.utils;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcUtilsTest {

    @Test
    void testSetMdcWithAllHeaders() {
        Message<String> message = MessageBuilder.withPayload("test")
                .setHeader("aws_messageId", "msg-001")
                .setHeader("X-Amzn-Trace-Id", "trace-001")
                .setHeader("iun", "IUN-001")
                .build();

        MdcUtils.setMdc(message);

        assertNotNull(MDC.get("trace_id"));
        MDC.clear();
    }

    @Test
    void testSetMdcWithoutOptionalHeaders() {
        Message<String> message = MessageBuilder.withPayload("test").build();

        MdcUtils.setMdc(message);

        // traceId deve essere generato (UUID)
        assertNotNull(MDC.get("trace_id"));
        assertNull(MDC.get("iun"));
        MDC.clear();
    }

    @Test
    void testSetMdcWithIunHeader() {
        Message<String> message = MessageBuilder.withPayload("test")
                .setHeader("iun", "IUN-SPECIAL")
                .build();

        MdcUtils.setMdc(message);

        assertNotNull(MDC.get("iun"));
        MDC.clear();
    }
}

