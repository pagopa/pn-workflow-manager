package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpecialAddressSearchStrategyTest {

    private final SpecialAddressSearchStrategy strategy = new SpecialAddressSearchStrategy();

    @Test
    void shouldReturnFoundForPec() {
        AddressSearchContext context = buildContext(ChannelType.PEC);

        SourceSearchOutcome outcome = strategy.search(context);

        assertTrue(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNotNull(outcome.address());
        assertEquals("user@pec.it", outcome.address().getAddress());
    }

    @Test
    void shouldReturnFoundForSms() {
        AddressSearchContext context = buildContext(ChannelType.SMS);

        SourceSearchOutcome outcome = strategy.search(context);

        assertTrue(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNotNull(outcome.address());
        assertEquals("3331234567", outcome.address().getAddress());
    }

    @Test
    void shouldReturnFoundForEmail() {
        AddressSearchContext context = buildContext(ChannelType.EMAIL);

        SourceSearchOutcome outcome = strategy.search(context);

        assertTrue(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNotNull(outcome.address());
        assertEquals("user@example.com", outcome.address().getAddress());
    }

    @Test
    void shouldReturnNotFoundForUnsupportedChannel() {
        AddressSearchContext context = buildContext(ChannelType.IO);

        SourceSearchOutcome outcome = strategy.search(context);

        assertFalse(outcome.found());
        assertTrue(outcome.tosAccepted());
        assertNull(outcome.address());
    }

    private AddressSearchContext buildContext(ChannelType channelType) {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .digitalDomicile(LegalDigitalAddressInt.builder().address("user@pec.it").build())
                .phoneNumber("3331234567")
                .email("user@example.com")
                .build();
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN")
                .sentAt(Instant.now())
                .recipients(List.of(recipient))
                .build();
        return new AddressSearchContext(channelType, Instant.now(), notification, 0, 0);
    }
}

