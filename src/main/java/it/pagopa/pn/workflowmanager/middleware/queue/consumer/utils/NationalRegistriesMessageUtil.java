package it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressSQSMessageDigitalAddressInner;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class NationalRegistriesMessageUtil {

    private NationalRegistriesMessageUtil(){}

    public static NationalRegistriesResponse buildPublicRegistryResponse(String correlationId, List<AddressSQSMessageDigitalAddressInner> digitalAddresses) {
        return NationalRegistriesResponse.builder()
                .correlationId(correlationId)
                .digitalAddress(mapToInformalDigitalAddressInt(digitalAddresses))
                .build();
    }

    private static InformalDigitalAddressInt mapToInformalDigitalAddressInt(List<AddressSQSMessageDigitalAddressInner> digitalAddresses) {
        if(CollectionUtils.isEmpty(digitalAddresses)) return null;

        return InformalDigitalAddressInt.builder()
                .address(digitalAddresses.get(0).getAddress())
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.valueOf(digitalAddresses.get(0).getType()))
                .build();
    }

}
