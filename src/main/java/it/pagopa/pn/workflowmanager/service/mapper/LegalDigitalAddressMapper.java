package it.pagopa.pn.workflowmanager.service.mapper;


import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;

public class LegalDigitalAddressMapper {

    private LegalDigitalAddressMapper(){}

    public static LegalDigitalAddressInt externalToInternal(LegalDigitalAddress legalDigitalAddress) {
        return LegalDigitalAddressInt.builder()
                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf(legalDigitalAddress.getChannelType().getValue()))
                .address(legalDigitalAddress.getValue())
                .build();
    }

    public static LegalDigitalAddress internalToExternal(LegalDigitalAddressInt digitalAddress) {
        LegalDigitalAddress legalDigitalAddress = new LegalDigitalAddress();
        legalDigitalAddress.setChannelType(
                LegalChannelType.valueOf(digitalAddress.getType().getValue())
            );
        legalDigitalAddress.setValue(digitalAddress.getAddress());
        
        return legalDigitalAddress;
    }
}
