package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.nationalregistries;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.api.AddressApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressOK;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@CustomLog
@RequiredArgsConstructor
@Component
public class PnNationalRegistriesClientImpl implements PnNationalRegistriesClient {
    private final AddressApi addressApi;
    private final PnWorkflowManagerConfigs cfg;

    @Override
    public AddressOK getAddresses(String recipientType, AddressRequestBody addressRequestBody) {
        log.logInvokingExternalService(CLIENT_NAME, GET_ADDRESSES);
        String cxId = cfg.getCxId();

        log.debug("[enter] getAddresses recipientType={} addressRequestBody={} cxId={}",
                recipientType,
                addressRequestBody,
                cxId);

        AddressOK response = addressApi.getAddresses(recipientType, addressRequestBody, cxId);

        log.debug("[exit] getAddresses recipientType={} addressRequestBody={} cxId={} response={}",
                recipientType,
                addressRequestBody,
                cxId,
                response);

        return response;
    }
}
