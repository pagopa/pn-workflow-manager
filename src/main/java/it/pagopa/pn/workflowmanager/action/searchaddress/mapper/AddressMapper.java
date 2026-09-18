package it.pagopa.pn.workflowmanager.action.searchaddress.mapper;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;

public class AddressMapper {

    private AddressMapper(){}

    public static InformalDigitalAddressInt externalToInternal(LegalDigitalAddressInt legalDigitalAddress) {
        return InformalDigitalAddressInt.builder()
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.valueOf(legalDigitalAddress.getType().getValue()))
                .address(legalDigitalAddress.getAddress())
                .build();
    }

    public static InformalDigitalAddressInt externalToInternal(CourtesyDigitalAddressInt courtesyDigitalAddress) {
        return InformalDigitalAddressInt.builder()
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.valueOf(courtesyDigitalAddress.getType().getValue()))
                .address(courtesyDigitalAddress.getAddress())
                .build();
    }
}
