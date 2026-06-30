package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.Instant;

class InformalDigitalDetailsIntTest {

    @Test
    void sendDigitalMessageToLog() {
        SendDigitalMessageDetailsInt details = SendDigitalMessageDetailsInt.builder()
                .recIndex(1)
                .digitalAddress(InformalDigitalAddressInt.builder().type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL).address("test@example.com").build())
                .digitalAddressSource(DigitalAddressSourceInt.PLATFORM)
                .channel(DigitalChannelsInt.EMAIL)
                .retryNumber(2)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d channel=%s digitalAddressSource=%s retryNumber=%s digitalAddress=%s",
                        1, DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.PLATFORM, 2, AuditLogUtils.SENSITIVE),
                details.toLog()
        );
        Assertions.assertEquals("test@example.com", details.getDigitalAddress().getAddress());
    }

    @Test
    void sendDigitalMessageFeedbackToLogAndTimestamp() {
        Instant notificationDate = Instant.parse("2024-02-01T10:00:00Z");
        SendDigitalMessageFeedbackDetailsInt details = SendDigitalMessageFeedbackDetailsInt.builder()
                .recIndex(2)
                .digitalAddress(InformalDigitalAddressInt.builder().address("pec@example.com").build())
                .digitalAddressSource(DigitalAddressSourceInt.GENERAL)
                .responseStatus(ResponseStatusInt.OK)
                .notificationDate(notificationDate)
                .channel(DigitalChannelsInt.PEC)
                .requestId("requestId")
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d responseStatus=%s requestId=%s channel=%s digitalAddressSource=%s deliveryDetail=%s digitalAddress=%s",
                        2, ResponseStatusInt.OK, "requestId", DigitalChannelsInt.PEC, DigitalAddressSourceInt.GENERAL, null, AuditLogUtils.SENSITIVE),
                details.toLog()
        );
        Assertions.assertEquals(DigitalAddressSourceInt.GENERAL, details.getDigitalAddressSource());
    }

    @Test
    void sendDigitalMessageProgressToLogAndTimestamp() {
        Instant eventTimestamp = Instant.parse("2024-02-02T11:00:00Z");
        SendDigitalMessageProgressDetailsInt details = SendDigitalMessageProgressDetailsInt.builder()
                .recIndex(3)
                .requestId("requestId")
                .digitalAddress(InformalDigitalAddressInt.builder().address("sms").build())
                .digitalAddressSource(DigitalAddressSourceInt.SPECIAL)
                .channel(DigitalChannelsInt.SMS)
                .retryNumber(1)
                .eventTimestamp(eventTimestamp)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d requestId=%s channel=%s digitalAddressSource=%s retryNumber=%s deliveryDetail=%s digitalAddress=%s eventTimestamp=%s",
                        3, "requestId", DigitalChannelsInt.SMS, DigitalAddressSourceInt.SPECIAL, 1, null, AuditLogUtils.SENSITIVE, eventTimestamp),
                details.toLog()
        );
    }

    @Test
    void sendDigitalMessageSkipToLog() {
        SendDigitalMessageSkipDetailsInt details = SendDigitalMessageSkipDetailsInt.builder()
                .recIndex(4)
                .channel(DigitalChannelsInt.APPIO)
                .digitalAddressSource(DigitalAddressSourceInt.PLATFORM)
                .retryNumber(5)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d channel=%s digitalAddressSource=%s retryNumber=%s",
                        4, DigitalChannelsInt.APPIO, DigitalAddressSourceInt.PLATFORM, 5),
                details.toLog()
        );
        Assertions.assertEquals(4, details.getRecIndex());
    }
}
