package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.api.CourtesyApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.api.LegalApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.api.ConsentsApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.CxTypeAuthFleet;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@CustomLog
@RequiredArgsConstructor
@Component
public class PnUserAttributesClientImpl implements PnUserAttributesClient {
    private final LegalApi legalApi;
    private final CourtesyApi courtesyApi;
    private final ConsentsApi consentsApi;

    @Override
    public List<LegalDigitalAddress> getLegalAddressBySender(String recipientId, String senderId) {
        log.logInvokingExternalService(CLIENT_NAME, GET_LEGAL_ADDRESS_BY_SENDER);
        return legalApi.getLegalAddressBySender(recipientId, senderId);
    }

    @Override
    public List<CourtesyDigitalAddress> getCourtesyAddressBySender(String recipientId, String senderId) {
        log.logInvokingExternalService(CLIENT_NAME, GET_COURTESY_ADDRESS_BY_SENDER);
        return courtesyApi.getCourtesyAddressBySender(recipientId, senderId);
    }

    @Override
    public List<Consent> getConsents(String xPagopaPnUid, CxTypeAuthFleet xPagopaPnCxType) {
        log.logInvokingExternalService(CLIENT_NAME, GET_CONSENTS);
        return consentsApi.getConsents(xPagopaPnUid, xPagopaPnCxType);
    }
}
