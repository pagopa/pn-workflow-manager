package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.informal.SendAnalogMessageFeedbackDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.informal.SendAnalogMessageProgressDetailsInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;

class InformalAnalogEventDetailsIntTest {

    @Test
    void sendAnalogMessageFeedbackToLogAndTimestamp() {
        Instant notificationDate = Instant.parse("2024-01-10T12:30:00Z");
        ServiceLevelInt serviceLevel = ServiceLevelInt.values()[0];
        SendAnalogMessageFeedbackDetailsInt details = SendAnalogMessageFeedbackDetailsInt.builder()
                .recIndex(2)
                .physicalAddress(PhysicalAddressInt.builder().address("address").build())
                .serviceLevel(serviceLevel)
                .sentAttemptMade(1)
                .newAddress(PhysicalAddressInt.builder().address("newAddress").build())
                .deliveryType(AnalogDeliveryTypeInt.RS)
                .responseStatus(ResponseStatusInt.OK)
                .requestTimelineId("requestTimelineId")
                .notificationDate(notificationDate)
                .attachments(List.of(AttachmentDetailsInt.builder().build()))
                .sendRequestId("sendRequestId")
                .registeredLetterCode("registeredLetterCode")
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d serviceLevel=%s sentAttemptMade=%s deliveryType=%s responseStatus=%s requestTimelineId=%s notificationDate=%s attachments=%s sendRequestId=%s registeredLetterCode=%s deliveryDetail=%s physicalAddress=%s newAddress=%s",
                        2, serviceLevel, 1, AnalogDeliveryTypeInt.RS, ResponseStatusInt.OK, "requestTimelineId", notificationDate,
                        List.of(AttachmentDetailsInt.builder().build()), "sendRequestId", "registeredLetterCode", null, AuditLogUtils.SENSITIVE, AuditLogUtils.SENSITIVE),
                details.toLog()
        );
        Assertions.assertEquals("newAddress", details.getNewAddress().getAddress());
        Assertions.assertEquals("address", details.getPhysicalAddress().getAddress());
    }

    @Test
    void sendAnalogMessageProgressToLogAndTimestamp() {
        Instant notificationDate = Instant.parse("2024-01-11T09:15:00Z");
        ServiceLevelInt serviceLevel = ServiceLevelInt.values()[0];
        SendAnalogMessageProgressDetailsInt details = SendAnalogMessageProgressDetailsInt.builder()
                .recIndex(4)
                .notificationDate(notificationDate)
                .deliveryType(AnalogDeliveryTypeInt.RS)
                .attachments(List.of())
                .sendRequestId("sendRequestId")
                .registeredLetterCode("registeredLetterCode")
                .serviceLevel(serviceLevel)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d notificationDate=%s deliveryType=%s deliveryDetail=%s attachments=%s sendRequestId=%s registeredLetterCode=%s serviceLevel=%s",
                        4, notificationDate, AnalogDeliveryTypeInt.RS, null, List.of(), "sendRequestId", "registeredLetterCode", serviceLevel),
                details.toLog()
        );
        Assertions.assertEquals(4, details.getRecIndex());
    }
}
