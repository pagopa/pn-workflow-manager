package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.consent.ConsentDto;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.ConsentType;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformPecAddressSearchStrategyTest {

    @Mock
    private PnUserAttributesClient userAttributesClient;
    @Mock
    private PnWorkflowManagerConfigs configs;
    @Mock
    private AddressBookService addressBookService;
    @InjectMocks
    private PlatformPecAddressSearchStrategy strategy;

    @Test
    void shouldReturnTosNotAcceptedWhenConsentIsMissing() {
        AddressSearchContext context = buildContext();
        when(addressBookService.areMandatoryConsentsAccepted(any(), any())).thenReturn(false);

        SourceSearchOutcome outcome = strategy.search(context);

        assertFalse(outcome.found());
        assertFalse(outcome.tosAccepted());
        assertNull(outcome.address());
    }

    @Test
    void shouldReturnNotFoundWhenConsentsOkAndNoPecAddress() {
        AddressSearchContext context = buildContext();
        when(addressBookService.areMandatoryConsentsAccepted(any(), any())).thenReturn(true);

        SourceSearchOutcome outcome = strategy.search(context);

        assertFalse(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNull(outcome.address());
    }

    @Test
    void shouldReturnFoundWhenConsentsOkAndPecAddressExists() {
        AddressSearchContext context = buildContext();
        when(addressBookService.getPlatformAddresses(any(), any())).thenReturn(Optional.of(
                LegalDigitalAddressInt.builder().type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC).build()
        ));

        when(addressBookService.areMandatoryConsentsAccepted(any(), any())).thenReturn(true);

        SourceSearchOutcome outcome = strategy.search(context);

        assertTrue(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNotNull(outcome.address());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC, outcome.address().getType());
    }

    private AddressSearchContext buildContext() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .internalId("PID")
                .recipientType(RecipientTypeInt.PF)
                .build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN")
                .sentAt(Instant.now())
                .sender(NotificationSenderInt.builder().paId("PA").build())
                .recipients(List.of(recipient))
                .build();
        return new AddressSearchContext(it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType.PEC, Instant.now(), notification, 0, 0);
    }

    private Consent validConsent() {
        return new Consent().consentType(ConsentType.TOS).consentVersion("1");
    }

    private ConsentDto validConfigConsent() {
        return new ConsentDto(ConsentType.TOS.getValue(), 1);
    }
}

