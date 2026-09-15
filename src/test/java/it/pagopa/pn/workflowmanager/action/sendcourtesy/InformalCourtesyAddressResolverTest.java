package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformalCourtesyAddressResolverTest {

    @Mock
    private AddressBookService addressBookService;

    @InjectMocks
    private InformalCourtesyAddressResolver resolver;

    private static final String INTERNAL_ID = "internalId";
    private static final String PA_ID = "paId";

    // ---------------- resolveAddresses ----------------

    @Test
    void resolveAddresses_shouldReturnEmptyList_whenAddressBookReturnsEmptyList() {
        // given
        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID)).thenReturn(Collections.emptyList());

        // when
        List<CourtesyDigitalAddressInt> result = resolver.resolveAddresses(INTERNAL_ID, PA_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveAddresses_shouldReturnOnlyEmailAndSms_whenListContainsMixedTypes() {
        // given
        CourtesyDigitalAddressInt emailAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, "email@example.com");
        CourtesyDigitalAddressInt smsAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS, "+390000000000");
        CourtesyDigitalAddressInt appIoAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.APPIO, "appio-address");
        CourtesyDigitalAddressInt tppAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.TPP, "tpp-address");

        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(emailAddress, smsAddress, appIoAddress, tppAddress));

        // when
        List<CourtesyDigitalAddressInt> result = resolver.resolveAddresses(INTERNAL_ID, PA_ID);

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(emailAddress));
        assertTrue(result.contains(smsAddress));
        assertTrue(!result.contains(appIoAddress) && !result.contains(tppAddress));
    }

    @Test
    void resolveAddresses_shouldReturnEmptyList_whenNoAddressHasAllowedType() {
        // given
        CourtesyDigitalAddressInt appIoAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.APPIO, "appio-address");
        CourtesyDigitalAddressInt tppAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.TPP, "tpp-address");

        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(appIoAddress, tppAddress));

        // when
        List<CourtesyDigitalAddressInt> result = resolver.resolveAddresses(INTERNAL_ID, PA_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveAddressByType_shouldReturnEmpty_whenAddressBookReturnsEmptyList() {
        // given
        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID)).thenReturn(Collections.emptyList());

        // when
        Optional<CourtesyDigitalAddressInt> result = resolver.resolveAddressByType(
                INTERNAL_ID, PA_ID, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveAddressByType_shouldReturnMatchingAddress_whenTypeIsPresent() {
        // given
        CourtesyDigitalAddressInt emailAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, "email@example.com");
        CourtesyDigitalAddressInt smsAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS, "+390000000000");

        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(emailAddress, smsAddress));

        // when
        Optional<CourtesyDigitalAddressInt> result = resolver.resolveAddressByType(
                INTERNAL_ID, PA_ID, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS);

        // then
        assertTrue(result.isPresent());
        assertEquals(smsAddress, result.get());
    }

    @Test
    void resolveAddressByType_shouldReturnEmpty_whenTypeIsNotPresent() {
        // given
        CourtesyDigitalAddressInt emailAddress = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, "email@example.com");

        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(emailAddress));

        // when
        Optional<CourtesyDigitalAddressInt> result = resolver.resolveAddressByType(
                INTERNAL_ID, PA_ID, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.APPIO);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void resolveAddressByType_shouldReturnFirstMatch_whenMultipleAddressesOfSameTypeExist() {
        // given
        CourtesyDigitalAddressInt firstEmail = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, "first@example.com");
        CourtesyDigitalAddressInt secondEmail = buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, "second@example.com");

        when(addressBookService.getCourtesyAddress(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(firstEmail, secondEmail));

        // when
        Optional<CourtesyDigitalAddressInt> result = resolver.resolveAddressByType(
                INTERNAL_ID, PA_ID, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);

        // then
        assertTrue(result.isPresent());
        assertEquals("first@example.com", result.get().getAddress());
    }

    private CourtesyDigitalAddressInt buildAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT type, String address) {
        return CourtesyDigitalAddressInt.builder()
                .type(type)
                .address(address)
                .build();
    }

}