package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.nationalregistries;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressOK;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody;

public interface PnNationalRegistriesClient {
    String CLIENT_NAME = "pn-national-registries";
    String GET_ADDRESSES = "GET ADDRESSES";

    AddressOK getAddresses(String recipientType, AddressRequestBody addressRequestBody);
}
