package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class AddressSearchRegistry {

    @Autowired
    private List<AddressSearchStrategy> registry;

    public AddressSearchStrategy find(DigitalAddressSourceInt source, ChannelType channel) {
        return registry.stream()
                .filter(strategy -> strategy.supportedKeys().stream().anyMatch(sourceChannelKey -> sourceChannelKey.channel().equals(channel) && sourceChannelKey.source().equals(source)))
                .findFirst()
                .orElse(null);
    }
}
