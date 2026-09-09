package it.pagopa.pn.workflowmanager.config.springbootcfg;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.addresssearch.ChannelAddressSourceRule;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

@Component
@AllArgsConstructor
public class ChannelAddressSourceConfigResolver {

    private final PnWorkflowManagerConfigs cfg;

    @PostConstruct
    public void validateRules(){
        Arrays.stream(ChannelType.values()).forEach(
                channel -> {
                    List<ChannelAddressSourceRule> rules = Optional.of(cfg.getAddressSearchMap()).map(map -> map.get(channel)).orElse(Collections.emptyList());

                    Set<Instant> validFromValues = new HashSet<>();
                    rules.forEach(rule -> {
                        if (CollectionUtils.isEmpty(rule.sources())) {
                            throw new IllegalArgumentException("Channel " + channel + " has a rule with empty or null sources for validFrom " + rule.validFrom());
                        }

                        if (!validFromValues.add(rule.validFrom())) {
                            throw new IllegalArgumentException("Channel " + channel + " has multiple rules with the same validFrom " + rule.validFrom());
                        }
                    });
                }

        );

    }

    public Optional<List<DigitalAddressSourceInt>> resolveSources(ChannelType channel, Instant sentAt) {
        return Optional.ofNullable(cfg.getAddressSearchMap().get(channel))
                .orElseGet(List::of)
                .stream()
                .filter(rule -> rule.validFrom().isBefore(sentAt))
                .max(Comparator.comparing(ChannelAddressSourceRule::validFrom))
                .map(ChannelAddressSourceRule::sources);
    }
}
