package it.pagopa.pn.workflowmanager.action.sendcourtesy.registry;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourtesyChannelSenderRegistryTest {

    @Test
    void getSenderReturnsRegisteredSender() {
        CourtesyAddressSender sender = new StubSender(CommunicationType.INFORMAL, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);
        CourtesyChannelSenderRegistry registry = new CourtesyChannelSenderRegistry(List.of(sender));

        assertSame(sender, registry.getSender(CommunicationType.INFORMAL, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL));
    }

    @Test
    void getSenderThrowsWhenSenderIsMissing() {
        CourtesyChannelSenderRegistry registry = new CourtesyChannelSenderRegistry(List.of());

        assertThrows(PnInternalException.class, () ->
                registry.getSender(CommunicationType.INFORMAL, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL));
    }

    private record StubSender(CommunicationType communicationType,
                              CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT courtesyAddressType) implements CourtesyAddressSender {
        @Override
        public CommunicationType getCommunicationType() {
            return communicationType;
        }

        @Override
        public CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT getCourtesyAddressType() {
            return courtesyAddressType;
        }

        @Override
        public CourtesySendOutcome send(NotificationInt notification, CourtesyDigitalAddressInt address, int recIndex) {
            return CourtesySendOutcome.SENT;
        }
    }
}
