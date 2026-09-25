package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.consent.ConsentDto;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.ConsentType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AddressBookServiceImplTest {
    private PnUserAttributesClient userAttributesClient;
    private AddressBookService addressBookService;
    private PnWorkflowManagerConfigs configs;

    private static final String RECIPIENT_ID = "recipientId";
    private static final String CX_ID = "PF";
    private static final String SENDER_ID = "senderId";

    @BeforeEach
    void setup() {
        userAttributesClient = Mockito.mock(PnUserAttributesClient.class);
        configs = Mockito.mock(PnWorkflowManagerConfigs.class);

        addressBookService = new AddressBookServiceImpl(
                userAttributesClient,
                configs
        );
    }

    @Test
    void getPlatformAddresses_shouldReturnEmpty_whenListIsNull() {
        // given
        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID)).thenReturn(null);

        // when
        Optional<LegalDigitalAddressInt> result = addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getPlatformAddresses_shouldReturnEmpty_whenListIsEmpty() {
        // given
        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID)).thenReturn(Collections.emptyList());

        // when
        Optional<LegalDigitalAddressInt> result = addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getPlatformAddresses_shouldReturnAddress_whenChannelTypeIsPec() {
        // given
        LegalDigitalAddress address = new LegalDigitalAddress();
        address.setChannelType(LegalChannelType.PEC);
        address.setValue("pec@example.com");
        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID)).thenReturn(List.of(address));

        // when
        Optional<LegalDigitalAddressInt> result = addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC, result.get().getType());
        assertEquals("pec@example.com", result.get().getAddress());
    }

    @Test
    void getPlatformAddresses_shouldReturnAddress_whenChannelTypeIsSercq() {
        // given
        LegalDigitalAddress address = new LegalDigitalAddress();
        address.setChannelType(LegalChannelType.SERCQ);
        address.setValue("sercq@example.com");
        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID)).thenReturn(List.of(address));

        // when
        Optional<LegalDigitalAddressInt> result = addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.SERCQ, result.get().getType());
        assertEquals("sercq@example.com", result.get().getAddress());
    }

    @Test
    void getPlatformAddresses_shouldReturnFirstMatchingAddress_whenMultipleElementsPresent() {
        // given
        LegalDigitalAddress firstAddress = new LegalDigitalAddress();
        firstAddress.setChannelType(LegalChannelType.PEC);
        firstAddress.setValue("first-pec@example.com");

        LegalDigitalAddress secondAddress = new LegalDigitalAddress();
        secondAddress.setChannelType(LegalChannelType.SERCQ);
        secondAddress.setValue("second-sercq@example.com");

        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID))
                .thenReturn(List.of(firstAddress, secondAddress));

        // when
        Optional<LegalDigitalAddressInt> result = addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID);

        // then
        assertTrue(result.isPresent());
        assertEquals("first-pec@example.com", result.get().getAddress());
    }

    @Test
    void getPlatformAddresses_shouldThrowException_whenChannelTypeIsNotSupportedByMapper() {
        // given
        LegalDigitalAddress address = new LegalDigitalAddress();
        address.setChannelType(LegalChannelType.APPIO);
        address.setValue("appio@example.com");
        when(userAttributesClient.getLegalAddressBySender(RECIPIENT_ID, SENDER_ID)).thenReturn(List.of(address));

        // when / then
        assertThrows(IllegalArgumentException.class,
                () -> addressBookService.getPlatformAddresses(RECIPIENT_ID, SENDER_ID));
    }


    @Test
    void getCourtesyAddress_shouldReturnMappedList_whenAddressesArePresent() {
        // given
        CourtesyDigitalAddress externalAddress1 = new CourtesyDigitalAddress();
        externalAddress1.setChannelType(CourtesyChannelType.EMAIL);
        externalAddress1.setValue("email@example.com");

        CourtesyDigitalAddress externalAddress2 = new CourtesyDigitalAddress();
        externalAddress2.setChannelType(CourtesyChannelType.SMS);
        externalAddress2.setValue("+390000000000");

        when(userAttributesClient.getCourtesyAddressBySender(RECIPIENT_ID, SENDER_ID))
                .thenReturn(List.of(externalAddress1, externalAddress2));

        // when
        List<CourtesyDigitalAddressInt> result = addressBookService.getCourtesyAddress(RECIPIENT_ID, SENDER_ID);

        // then
        assertEquals(2, result.size());
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, result.get(0).getType());
        assertEquals("email@example.com", result.get(0).getAddress());
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS, result.get(1).getType());
        assertEquals("+390000000000", result.get(1).getAddress());
    }

    @Test
    void getCourtesyAddress_shouldReturnSingleMappedAddress_whenListHasOneElement() {
        // given
        CourtesyDigitalAddress externalAddress = new CourtesyDigitalAddress();
        externalAddress.setChannelType(CourtesyChannelType.APPIO);
        externalAddress.setValue("appio-address");

        when(userAttributesClient.getCourtesyAddressBySender(RECIPIENT_ID, SENDER_ID))
                .thenReturn(List.of(externalAddress));

        // when
        List<CourtesyDigitalAddressInt> result = addressBookService.getCourtesyAddress(RECIPIENT_ID, SENDER_ID);

        // then
        assertEquals(1, result.size());
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.APPIO, result.getFirst().getType());
        assertEquals("appio-address", result.getFirst().getAddress());
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenConsentsListIsNullAndThereAreConsentsConfigured() {
        // given
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(null);
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(ConsentDto.builder().type(ConsentType.TOS.getValue()).version(1).build()));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted("PF-MAnu", CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenConsentsListIsEmptyAndThereAreConsentsConfigured() {
        // given
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(Collections.emptyList());
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(ConsentDto.builder().type(ConsentType.TOS.getValue()).version(1).build()));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnNull_whenConsentsListIsEmptyAndThereAreNoConsentsConfigured() {
        // given
        when(configs.getConsentsForPlatformSearch()).thenReturn(Collections.emptyList());

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertNull(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenNoConsentMatchesConfigType() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "1", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.DATAPRIVACY.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenConsentTypeMatchesButNotAccepted() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "1", false);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenConsentVersionIsLowerThanConfigVersion() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "1", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(2).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenConsentVersionsAreNotComparable() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "a1", true);
        Consent consent2 = buildConsent(ConsentType.TOS, "default", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent, consent2));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(2).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnTrue_whenConsentMatchesConfigTypeVersionAndIsAccepted() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "1", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertTrue(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnTrue_whenConsentVersionIsHigherThanConfigVersionAndAccepted() {
        // given
        Consent consent = buildConsent(ConsentType.TOS, "3", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(consent));

        ConsentDto configConsent = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertTrue(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnFalse_whenOnlySomeMandatoryConsentsAreSatisfied() {
        // given
        Consent tosConsent = buildConsent(ConsentType.TOS, "2", true);
        when(userAttributesClient.getConsents(anyString(), any())).thenReturn(List.of(tosConsent));

        ConsentDto configConsent1 = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(2).build();
        ConsentDto configConsent2 = ConsentDto.builder().type(ConsentType.DATAPRIVACY.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent1, configConsent2));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertFalse(result);
    }

    @Test
    void areMandatoryConsentsAccepted_shouldReturnTrue_whenAllMandatoryConsentsAreSatisfiedByDifferentConsents() {
        // given
        Consent tosConsent = buildConsent(ConsentType.TOS, "2", true);
        Consent dataPrivacyConsent = buildConsent(ConsentType.DATAPRIVACY, "1", true);
        when(userAttributesClient.getConsents(anyString(), any()))
                .thenReturn(List.of(tosConsent, dataPrivacyConsent));

        ConsentDto configConsent1 = ConsentDto.builder().type(ConsentType.TOS.getValue()).version(2).build();
        ConsentDto configConsent2 = ConsentDto.builder().type(ConsentType.DATAPRIVACY.getValue()).version(1).build();
        when(configs.getConsentsForPlatformSearch()).thenReturn(List.of(configConsent1, configConsent2));

        // when
        Boolean result = addressBookService.areMandatoryConsentsAccepted(RECIPIENT_ID, CX_ID);

        // then
        assertTrue(result);
    }

    private Consent buildConsent(ConsentType type, String version, boolean accepted) {
        Consent consent = new Consent();
        consent.setConsentType(type);
        consent.setConsentVersion(version);
        consent.setAccepted(accepted);
        return consent;
    }
}