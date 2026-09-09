package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.CxTypeAuthFleet;

import java.util.List;

public interface PnUserAttributesClient {
    String CLIENT_NAME = "pn-user-attributes";
    String GET_LEGAL_ADDRESS_BY_SENDER = "GET LEGAL ADDRESS BY SENDER";
    String GET_COURTESY_ADDRESS_BY_SENDER = "GET COURTESY ADDRESS BY SENDER";
    String GET_CONSENTS = "GET CONSENTS";

    List<LegalDigitalAddress> getLegalAddressBySender(String recipientId, String senderId);

    List<CourtesyDigitalAddress> getCourtesyAddressBySender(String recipientId, String senderId);

    List<Consent> getConsents(String xPagopaPnUid, CxTypeAuthFleet xPagopaPnCxType);
}
