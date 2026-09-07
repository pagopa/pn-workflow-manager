package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.nationalregistries;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.api.AddressApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressOK;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressRequestBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnNationalRegistriesClientImplTest {

    @Mock
    private AddressApi addressApi;

    @Mock
    private PnWorkflowManagerConfigs cfg;

    @InjectMocks
    private PnNationalRegistriesClientImpl pnNationalRegistriesClient;

    @Test
    void getAddresses_shouldDelegateToAddressApiWithConfiguredCxId() {
        AddressRequestBody request = new AddressRequestBody();
        AddressOK expected = new AddressOK();
        when(cfg.getCxId()).thenReturn("cx-123");
        when(addressApi.getAddresses("PF", request, "cx-123")).thenReturn(expected);

        AddressOK result = pnNationalRegistriesClient.getAddresses("PF", request);

        assertEquals(expected, result);
        verify(addressApi).getAddresses("PF", request, "cx-123");
    }
}
