package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

class InformalAnalogDetailsIntTest {

    @Test
    void prepareAnalogDeliveryToLog() {
        ServiceLevelInt serviceLevel = ServiceLevelInt.values()[0];
        PrepareAnalogDeliveryDetailsInt details = PrepareAnalogDeliveryDetailsInt.builder()
                .recIndex(1)
                .physicalAddress(PhysicalAddressInt.builder().address("address").build())
                .serviceLevel(serviceLevel)
                .deliveryType(AnalogDeliveryTypeInt.RS)
                .sentAttemptMade(2)
                .relatedRequestId("requestId")
                .foreignState("IT")
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d serviceLevel=%s deliveryType=%s sentAttemptMade=%s relatedRequestId=%s foreignState=%s physicalAddress=%s",
                        1, serviceLevel, AnalogDeliveryTypeInt.RS, 2, "requestId", "IT", AuditLogUtils.SENSITIVE),
                details.toLog()
        );
        Assertions.assertEquals(1, details.getRecIndex());
        Assertions.assertEquals("address", details.getPhysicalAddress().getAddress());
    }

    @Test
    void sendAnalogMessageToLog() {
        ServiceLevelInt serviceLevel = ServiceLevelInt.values()[0];
        SendAnalogMessageDetailsInt details = SendAnalogMessageDetailsInt.builder()
                .recIndex(3)
                .physicalAddress(PhysicalAddressInt.builder().address("address").build())
                .serviceLevel(serviceLevel)
                .deliveryType(AnalogDeliveryTypeInt.RS)
                .sentAttemptMade(4)
                .relatedRequestId("relatedRequestId")
                .foreignState("FR")
                .productType("AR")
                .analogCost(100)
                .numberOfPages(12)
                .envelopeWeight(50)
                .prepareRequestId("prepareRequestId")
                .f24Attachments(List.of("f24"))
                .categorizedAttachmentsResult(CategorizedAttachmentsResultInt.builder().build())
                .vat(22)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d serviceLevel=%s deliveryType=%s sentAttemptMade=%s relatedRequestId=%s foreignState=%s productType=%s analogCost=%s numberOfPages=%s envelopeWeight=%s prepareRequestId=%s f24Attachments=%s vat=%s physicalAddress=%s",
                        3, serviceLevel, AnalogDeliveryTypeInt.RS, 4, "relatedRequestId", "FR", "AR", 100, 12, 50, "prepareRequestId", List.of("f24"), 22, AuditLogUtils.SENSITIVE),
                details.toLog()
        );
        Assertions.assertEquals(3, details.getRecIndex());
        Assertions.assertEquals("AR", details.getProductType());
        Assertions.assertEquals("address", details.getPhysicalAddress().getAddress());
    }
}
