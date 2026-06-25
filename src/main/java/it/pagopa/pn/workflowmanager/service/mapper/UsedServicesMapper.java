package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.UsedServices;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.UsedServicesInt;

public class UsedServicesMapper {
    private UsedServicesMapper() {
    }

    public static UsedServicesInt externalToInternal(UsedServices external) {
        return external != null ? UsedServicesInt.builder()
                .physicalAddressLookUp(external.getPhysicalAddressLookup())
                .build() : null;
    }
}
