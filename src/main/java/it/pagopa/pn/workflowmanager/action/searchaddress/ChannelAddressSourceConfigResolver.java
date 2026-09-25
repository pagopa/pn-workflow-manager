package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class ChannelAddressSourceConfigResolver {

    private final SearchDigitalDomicileParameterConsumer parameterConsumer;
    private final AddressSearchRegistry addressSearchRegistry;

    @PostConstruct
    public void validateRules() {
        List<SearchDigitalDomicileConfig> configs = parameterConsumer.getSearchDigitalDomicileConfigs();
        Set<Instant> validFromValues = new HashSet<>();

        configs.forEach(config -> {
            if (config.getValidFrom() == null) {
                throw new IllegalArgumentException("Search digital domicile configuration has null validFrom");
            }
            if (!validFromValues.add(config.getValidFrom())) {
                throw new IllegalArgumentException("Multiple search digital domicile configurations have the same validFrom " + config.getValidFrom());
            }

            validateChannelSources(config.getValidFrom(), ChannelType.PEC, config.getPec());
            validateChannelSources(config.getValidFrom(), ChannelType.SMS, config.getSms());
            validateChannelSources(config.getValidFrom(), ChannelType.EMAIL, config.getEmail());
        });
    }

    public List<DigitalAddressSourceInt> resolveSources(ChannelType channel, Instant sentAt) {
        return parameterConsumer.getSearchDigitalDomicileConfigs()
                .stream()
                .filter(config -> !config.getValidFrom().isAfter(sentAt))
                .max(Comparator.comparing(SearchDigitalDomicileConfig::getValidFrom))
                .map(config -> getSourcesByChannel(config, channel))
                .orElse(List.of(DigitalAddressSourceInt.SPECIAL)); // In assenza di sorgenti configurate per canale, effettuiamo di default una ricerca SPECIAL
    }

    private void validateChannelSources(Instant validFrom, ChannelType channel, List<DigitalAddressSourceInt> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("Search digital domicile configuration has null " + channel.name().toLowerCase() + " sources for validFrom " + validFrom);
        }
        Set<DigitalAddressSourceInt> uniqueSources = new HashSet<>();
        sources.forEach(source -> {
            if (source == null) {
                throw new IllegalArgumentException("Search digital domicile configuration has null " + channel.name().toLowerCase() + " source for validFrom " + validFrom);
            }
            if (!uniqueSources.add(source)) {
                throw new IllegalArgumentException("Channel " + channel + " has duplicated source " + source + " for validFrom " + validFrom);
            }
            if (!isSupported(channel, source)) {
                throw new IllegalArgumentException("Channel " + channel + " has unsupported source " + source + " for validFrom " + validFrom);
            }
        });
    }

    /*
        In caso di configurazioni vuote si va di default con SPECIAL.
     */
    private List<DigitalAddressSourceInt> getSourcesByChannel(SearchDigitalDomicileConfig config, ChannelType channel) {
        return switch (channel) {
            case PEC -> config.getPec().isEmpty() ? List.of(DigitalAddressSourceInt.SPECIAL) : config.getPec();
            case SMS -> config.getSms().isEmpty() ? List.of(DigitalAddressSourceInt.SPECIAL) : config.getSms();
            case EMAIL -> config.getEmail().isEmpty() ? List.of(DigitalAddressSourceInt.SPECIAL) : config.getEmail();
            default -> List.of();
        };
    }

    private boolean isSupported(ChannelType channel, DigitalAddressSourceInt source) {
        return addressSearchRegistry.find(source, channel) != null;
    }
}
