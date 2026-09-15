package it.pagopa.pn.workflowmanager.action.searchaddress.mapper;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;

public class AddressMapper {

    private AddressMapper(){}

    public static InformalDigitalAddressInt externalToInternal(LegalDigitalAddress legalDigitalAddress) {
        return InformalDigitalAddressInt.builder()
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.valueOf(legalDigitalAddress.getChannelType().getValue()))
                .address(legalDigitalAddress.getValue())
                .build();
    }

    public static InformalDigitalAddressInt externalToInternal(CourtesyDigitalAddress courtesyDigitalAddress) {
        return InformalDigitalAddressInt.builder()
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.valueOf(courtesyDigitalAddress.getChannelType().getValue()))
                .address(courtesyDigitalAddress.getValue())
                .build();
    }
}
