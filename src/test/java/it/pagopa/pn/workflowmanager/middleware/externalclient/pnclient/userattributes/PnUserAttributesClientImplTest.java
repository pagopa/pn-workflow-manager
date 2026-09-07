package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.ConsentsApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.CourtesyApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.api.LegalApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.model.CxTypeAuthFleet;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.model.LegalDigitalAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnUserAttributesClientImplTest {

    @Mock
    private LegalApi legalApi;

    @Mock
    private CourtesyApi courtesyApi;

    @Mock
    private ConsentsApi consentsApi;

    @InjectMocks
    private PnUserAttributesClientImpl pnUserAttributesClient;

    @Test
    void getLegalAddressBySender_shouldDelegateToLegalApi() {
        List<LegalDigitalAddress> expected = List.of(new LegalDigitalAddress());
        when(legalApi.getLegalAddressBySender("recipientId", "senderId")).thenReturn(expected);

        List<LegalDigitalAddress> result = pnUserAttributesClient.getLegalAddressBySender("recipientId", "senderId");

        assertEquals(expected, result);
        verify(legalApi).getLegalAddressBySender("recipientId", "senderId");
    }

    @Test
    void getCourtesyAddressBySender_shouldDelegateToCourtesyApi() {
        List<CourtesyDigitalAddress> expected = List.of(new CourtesyDigitalAddress());
        when(courtesyApi.getCourtesyAddressBySender("recipientId", "senderId")).thenReturn(expected);

        List<CourtesyDigitalAddress> result = pnUserAttributesClient.getCourtesyAddressBySender("recipientId", "senderId");

        assertEquals(expected, result);
        verify(courtesyApi).getCourtesyAddressBySender("recipientId", "senderId");
    }

    @Test
    void getConsents_shouldDelegateToConsentsApi() {
        List<Consent> expected = List.of(new Consent());
        when(consentsApi.getConsents("userId", CxTypeAuthFleet.PF)).thenReturn(expected);

        List<Consent> result = pnUserAttributesClient.getConsents("userId", CxTypeAuthFleet.PF);

        assertEquals(expected, result);
        verify(consentsApi).getConsents("userId", CxTypeAuthFleet.PF);
    }
}
