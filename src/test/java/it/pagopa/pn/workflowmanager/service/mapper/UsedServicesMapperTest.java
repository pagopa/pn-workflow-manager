package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.UsedServices;

import it.pagopa.pn.workflowmanager.dto.notification.common.UsedServicesInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UsedServicesMapperTest {
    @Test
    void testExternalToInternal() {
        UsedServices external = new UsedServices();
        external.setPhysicalAddressLookup(true);

        UsedServicesInt internal = UsedServicesMapper.externalToInternal(external);

        Assertions.assertNotNull(internal);
        Assertions.assertTrue(internal.getPhysicalAddressLookUp());
    }

    @Test
    void testExternalToInternalWithNull() {
        UsedServices external = null;
        UsedServicesInt internal = UsedServicesMapper.externalToInternal(external);

        Assertions.assertNull(internal);
    }
}
