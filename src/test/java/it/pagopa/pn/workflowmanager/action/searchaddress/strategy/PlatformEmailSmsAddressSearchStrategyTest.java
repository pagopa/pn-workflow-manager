package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.InformalCourtesyAddressResolver;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
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
class PlatformEmailSmsAddressSearchStrategyTest {

    @Mock
    private AddressBookService addressBookService;
    @Mock
    private InformalCourtesyAddressResolver informalCourtesyAddressResolver;

    @InjectMocks
    private PlatformEmailSmsAddressSearchStrategy strategy;

    @Test
    void shouldReturnTosNotAcceptedWhenConsentIsMissing() {
        AddressSearchContext context = buildContext(ChannelType.EMAIL);

        SourceSearchOutcome outcome = strategy.search(context);

        assertFalse(outcome.found());
        assertFalse(outcome.tosAccepted());
        assertNull(outcome.address());
    }

    @Test
    void shouldReturnNotFoundWhenConsentsOkAndNoCourtesyAddress() {
        AddressSearchContext context = buildContext(ChannelType.SMS);
        when(addressBookService.areMandatoryConsentsAccepted(any(), any())).thenReturn(true);

        SourceSearchOutcome outcome = strategy.search(context);

        assertFalse(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNull(outcome.address());
    }

    @Test
    void shouldReturnFoundWhenConsentsOkAndCourtesyAddressExists() {
        AddressSearchContext context = buildContext(ChannelType.EMAIL);
        when(informalCourtesyAddressResolver.resolveAddressByType(any(), any(), any())).thenReturn(Optional.of(
                CourtesyDigitalAddressInt.builder().type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL).build()
        ));
        when(addressBookService.areMandatoryConsentsAccepted(any(), any())).thenReturn(true);
        SourceSearchOutcome outcome = strategy.search(context);

        assertTrue(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNotNull(outcome.address());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL, outcome.address().getType());
    }

    private AddressSearchContext buildContext(ChannelType channelType) {
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
        return new AddressSearchContext(channelType, Instant.now(), notification, 0, 0);
    }
}
