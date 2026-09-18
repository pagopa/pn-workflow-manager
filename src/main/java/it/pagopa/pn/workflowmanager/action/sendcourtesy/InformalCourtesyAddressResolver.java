package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@CustomLog
public class InformalCourtesyAddressResolver {
    private final AddressBookService addressBookService;
    private final static List<CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT> ALLOWED_TYPES = List.of(
            CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL,
            CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS
    );

    public List<CourtesyDigitalAddressInt> resolveAddresses(String internalId, String paId) {
        return addressBookService.getCourtesyAddress(internalId, paId).stream()
                .filter(address -> ALLOWED_TYPES.contains(address.getType()))
                .toList();
    }

    public Optional<CourtesyDigitalAddressInt> resolveAddressByType(String internalId, String paId, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT type) {
        return addressBookService.getCourtesyAddress(internalId, paId).stream()
                .filter(address -> address.getType() == type)
                .findFirst();
    }
}
