package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;

import java.util.List;

public record AddressSearchPlan(
        String iun,
        int recIndex,
        ChannelType channel,
        int attempt,
        List<DigitalAddressSourceInt> orderedSources) {
}
