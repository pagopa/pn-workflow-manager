package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.UsedServicesInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.delivery.model.UsedServices;

public class UsedServicesMapper {
    private UsedServicesMapper() {
    }

    public static UsedServicesInt externalToInternal(UsedServices external) {
        return external != null ? UsedServicesInt.builder()
                .physicalAddressLookUp(external.getPhysicalAddressLookup())
                .build() : null;
    }
}
