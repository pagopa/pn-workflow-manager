package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.ChannelSourceRule;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ChannelAddressSourceConfigResolver {
    private final PnWorkflowManagerConfigs cfg;
    public Optional<List<DigitalAddressSourceInt>> resolveSources(ChannelType channel, Instant referenceDate) {
        List<ChannelSourceRule> rules = cfg.getAddressSearchMap().get(channel);

        return rules.stream()
                .filter(r -> !r.validFrom().isAfter(referenceDate))
                .max(Comparator.comparing(ChannelSourceRule::validFrom))
                .map(ChannelSourceRule::sources); // Default to SPECIAL if no valid rule is found
    }
}
