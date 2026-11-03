package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import it.pagopa.pn.api.dto.events.PnNoticePaymentPaidEvent;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.NoticePaymentEventHandler;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.validator.NoticePaymentPaidEventValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class NoticePaymentEventConsumerTest {

    private final NoticePaymentEventHandler handler = mock(NoticePaymentEventHandler.class);
    private final NoticePaymentEventConsumer consumer = new NoticePaymentEventConsumer(
            new PnWorkflowManagerConfigs(),
            new NoticePaymentPaidEventValidator(),
            handler);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void noticePaymentEventConsumerMapsAndHandlesValidEvent() {
        consumer.noticePaymentEventConsumer(message(validEvent()));

        verify(handler).handleResponse(argThat(event ->
                "IUN-001".equals(event.getIun())
                        && event.getRecIndex() == 0
                        && "NOTICE-001".equals(event.getNoticeCode())
                        && "TAX-ID".equals(event.getCreditorTaxId())
                        && "IUV-001".equals(event.getIuv())
                        && Integer.valueOf(1200).equals(event.getAmount())
        ));
    }

    @Test
    void noticePaymentEventConsumerDoesNotCallHandlerForInvalidEvent() {
        PnNoticePaymentPaidEvent invalidEvent = PnNoticePaymentPaidEvent.builder()
                .payload(PnNoticePaymentPaidEvent.Payload.builder().iun("IUN-001").recIndex(-1).build())
                .build();

        consumer.noticePaymentEventConsumer(message(invalidEvent));

        verifyNoInteractions(handler);
    }

    @Test
    void noticePaymentEventConsumerPropagatesHandlerFailure() {
        doThrow(new IllegalStateException("handler failure")).when(handler).handleResponse(any());

        assertThrows(IllegalStateException.class, () -> consumer.noticePaymentEventConsumer(message(validEvent())));

        verify(handler).handleResponse(any(NoticePaymentInt.class));
    }

    private Message<PnNoticePaymentPaidEvent> message(PnNoticePaymentPaidEvent event) {
        return MessageBuilder.withPayload(event).build();
    }

    private PnNoticePaymentPaidEvent validEvent() {
        return PnNoticePaymentPaidEvent.builder()
                .payload(PnNoticePaymentPaidEvent.Payload.builder()
                        .iun("IUN-001")
                        .recIndex(0)
                        .noticeCode("NOTICE-001")
                        .creditorTaxId("TAX-ID")
                        .iuv("IUV-001")
                        .amount(1200)
                        .paymentDateTime(Instant.parse("2026-10-28T12:00:00Z"))
                        .build())
                .build();
    }
}
