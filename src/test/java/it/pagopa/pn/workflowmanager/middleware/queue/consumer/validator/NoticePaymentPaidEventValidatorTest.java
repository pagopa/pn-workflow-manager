package it.pagopa.pn.workflowmanager.middleware.queue.consumer.validator;

import it.pagopa.pn.api.dto.events.PnNoticePaymentPaidEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoticePaymentPaidEventValidatorTest {

    private final NoticePaymentPaidEventValidator validator = new NoticePaymentPaidEventValidator();

    @Test
    void validateReturnsRequiredErrorsWhenPayloadIsMissing() {
        PnNoticePaymentPaidEvent.Payload event = null;

        assertEquals(List.of("payload is required"), validator.validate(event));
    }

    @Test
    void validateReturnsAllRequiredFieldErrors() {
        PnNoticePaymentPaidEvent.Payload payload = PnNoticePaymentPaidEvent.Payload.builder()
                .iun(" ")
                .recIndex(null)
                .noticeCode("")
                .creditorTaxId(null)
                .build();

        assertEquals(List.of(
                "iun is required",
                "recIndex is required",
                "noticeCode is required",
                "creditorTaxId is required"
        ), validator.validate(payload));
    }

    @Test
    void validateRejectsNegativeRecipientIndex() {
        PnNoticePaymentPaidEvent.Payload payload = validPayload().recIndex(-1).build();

        assertEquals(List.of("recIndex must be non-negative"),
                validator.validate(payload));
    }

    @Test
    void validateAcceptsCompletePayloadWithZeroRecipientIndex() {
        PnNoticePaymentPaidEvent.Payload payload = validPayload().recIndex(0).build();

        assertEquals(List.of(), validator.validate(payload));
    }

    private PnNoticePaymentPaidEvent.Payload.PayloadBuilder validPayload() {
        return PnNoticePaymentPaidEvent.Payload.builder()
                .iun("IUN-001")
                .noticeCode("NOTICE-001")
                .creditorTaxId("TAX-ID");
    }
}
