package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourtesyDigitalAddressMapperTest {
    @Test
    void externalToInternal() {

        CourtesyDigitalAddressInt actual = CourtesyDigitalAddressMapper.externalToInternal(buildCourtesyDigitalAddress());

        Assertions.assertEquals(buildCourtesyDigitalAddressInt(), actual);
    }

    @Test
    void internalToExternal() {

        CourtesyDigitalAddress actual = CourtesyDigitalAddressMapper.internalToExternal(buildCourtesyDigitalAddressInt());

        Assertions.assertEquals(buildCourtesyDigitalAddressInt().getAddress(), actual.getValue());
    }

    private CourtesyDigitalAddress buildCourtesyDigitalAddress() {
        CourtesyDigitalAddress address = new CourtesyDigitalAddress();
        address.setValue("001");
        address.setChannelType(CourtesyChannelType.EMAIL);
        return address;
    }

    private CourtesyDigitalAddressInt buildCourtesyDigitalAddressInt() {
        return CourtesyDigitalAddressInt.builder()
                .address("001")
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .build();
    }
}