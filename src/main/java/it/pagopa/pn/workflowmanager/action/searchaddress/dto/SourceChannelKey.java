package it.pagopa.pn.workflowmanager.action.searchaddress.dto;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;

public record SourceChannelKey(DigitalAddressSourceInt source, ChannelType channel) {}

