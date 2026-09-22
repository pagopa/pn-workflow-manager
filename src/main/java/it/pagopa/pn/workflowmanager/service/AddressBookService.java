package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;

import java.util.List;
import java.util.Optional;

public interface AddressBookService {
    Optional<LegalDigitalAddressInt> getPlatformAddresses(String internalId, String senderId);

    List<CourtesyDigitalAddressInt> getCourtesyAddress(String recipientId, String senderId);

    boolean areMandatoryConsentsAccepted(String recipientId, String cxId);
}
