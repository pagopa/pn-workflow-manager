package it.pagopa.pn.workflowmanager.action.sendcourtesy.sender;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface CourtesyAddressSender {
    CommunicationType getCommunicationType();
    CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT getCourtesyAddressType();
    CourtesySendOutcome send(NotificationInt notification, CourtesyDigitalAddressInt address, int recIndex);
}
