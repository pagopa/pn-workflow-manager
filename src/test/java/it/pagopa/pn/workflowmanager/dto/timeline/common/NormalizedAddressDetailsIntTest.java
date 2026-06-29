package it.pagopa.pn.workflowmanager.dto.timeline.common;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.NormalizedAddressDetailsInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormalizedAddressDetailsIntTest {

    private NormalizedAddressDetailsInt normalizedAddressDetails;

    @BeforeEach
    void setup() {
        PhysicalAddressInt oldAddress = PhysicalAddressInt.builder()
                .fullname("Mario Rossi")
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .build();

        PhysicalAddressInt normalizedAddress = PhysicalAddressInt.builder()
                .fullname("Mario Rossi")
                .address("Via Roma 1")
                .addressDetails("Scala A")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .build();

        normalizedAddressDetails = NormalizedAddressDetailsInt.builder()
                .recIndex(0)
                .oldAddress(oldAddress)
                .normalizedAddress(normalizedAddress)
                .build();
    }

    @Test
    void toLog() {
        String log = normalizedAddressDetails.toLog();
        Assertions.assertEquals(
                String.format("recIndex=%d oldPhysicalAddress=%s normalizedAddress=%s",0,AuditLogUtils.SENSITIVE,AuditLogUtils.SENSITIVE),
                log
        );
    }

    @Test
    void getRecIndex() {
        int actualRecIndex = normalizedAddressDetails.getRecIndex();
        Assertions.assertEquals(0, actualRecIndex);
    }

    @Test
    void getOldAddress() {
        PhysicalAddressInt actualOldAddress = normalizedAddressDetails.getOldAddress();
        Assertions.assertNotNull(actualOldAddress);
        Assertions.assertEquals("Via Roma 1", actualOldAddress.getAddress());
    }

    @Test
    void getNormalizedAddress() {
        PhysicalAddressInt actualNormalizedAddress = normalizedAddressDetails.getNormalizedAddress();
        Assertions.assertNotNull(actualNormalizedAddress);
        Assertions.assertEquals("Scala A", actualNormalizedAddress.getAddressDetails());
    }

    @Test
    void getNewAddress() {
        PhysicalAddressInt actualNewAddress = normalizedAddressDetails.getNewAddress();
        Assertions.assertNotNull(actualNewAddress);
        Assertions.assertEquals(normalizedAddressDetails.getNormalizedAddress(), actualNewAddress);
    }

    @Test
    void setNewAddress() {
        PhysicalAddressInt newAddress = PhysicalAddressInt.builder()
                .fullname("Luigi Verdi")
                .address("Via Milano 10")
                .build();

        normalizedAddressDetails.setNewAddress(newAddress);
        Assertions.assertEquals(newAddress, normalizedAddressDetails.getNormalizedAddress());
    }

    @Test
    void getPhysicalAddress() {
        PhysicalAddressInt actualPhysicalAddress = normalizedAddressDetails.getPhysicalAddress();
        Assertions.assertNotNull(actualPhysicalAddress);
        Assertions.assertEquals(normalizedAddressDetails.getOldAddress(), actualPhysicalAddress);
    }

    @Test
    void setPhysicalAddress() {
        PhysicalAddressInt newPhysicalAddress = PhysicalAddressInt.builder()
                .fullname("Giuseppe Bianchi")
                .address("Via Napoli 5")
                .build();

        normalizedAddressDetails.setPhysicalAddress(newPhysicalAddress);
        Assertions.assertEquals(newPhysicalAddress, normalizedAddressDetails.getOldAddress());
    }
}
