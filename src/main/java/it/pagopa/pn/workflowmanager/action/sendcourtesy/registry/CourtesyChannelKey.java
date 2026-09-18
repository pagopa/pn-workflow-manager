package it.pagopa.pn.workflowmanager.action.sendcourtesy.registry;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;

public record CourtesyChannelKey(CommunicationType communicationType, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channelType) {}

