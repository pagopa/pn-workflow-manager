package it.pagopa.pn.workflowmanager.service.mapper;


import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;

public class CourtesyDigitalAddressMapper {
    private CourtesyDigitalAddressMapper(){}
    
    public static CourtesyDigitalAddressInt externalToInternal(CourtesyDigitalAddress courtesyDigitalAddress) {
        return CourtesyDigitalAddressInt.builder()
                .address(courtesyDigitalAddress.getValue())
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.valueOf(courtesyDigitalAddress.getChannelType().getValue()))
                .build();
    }

    public static CourtesyDigitalAddress internalToExternal(CourtesyDigitalAddressInt digitalAddress) {
        CourtesyDigitalAddress courtesyDigitalAddress = new CourtesyDigitalAddress();
        courtesyDigitalAddress.setValue(digitalAddress.getAddress());
        courtesyDigitalAddress.setChannelType(CourtesyChannelType.valueOf(digitalAddress.getType().getValue()));
        return courtesyDigitalAddress;
    }

}
