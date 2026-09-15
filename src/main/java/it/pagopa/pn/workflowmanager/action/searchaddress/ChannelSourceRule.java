package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;

import java.time.Instant;
import java.util.List;

public record ChannelSourceRule(Instant validFrom, List<DigitalAddressSourceInt> sources) {}