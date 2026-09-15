package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AddressSearchOrchestrator {
    private final ChannelAddressSourceConfigResolver configResolver;
    private final AddressSearchProgressor progressor;
    private final AddressSearchUtils searchUtils;

    public void handle(AddressSearchContext ctx) {
        Optional<List<DigitalAddressSourceInt>> optSources = configResolver.resolveSources(ctx.channel(), ctx.sentAt());

        if(ChannelType.IO.equals(ctx.channel()) || ChannelType.ANALOG.equals(ctx.channel())) {
            // Se non ci sono sorgenti configurate per il canale, schedulo direttamente l'azione di invio con source SPECIAL.
            searchUtils.scheduleSendChannelMessageAction(ctx, DigitalAddressSourceInt.SPECIAL);
            return;
        }

        if (optSources.isEmpty()) {
            optSources = Optional.of(List.of(DigitalAddressSourceInt.SPECIAL));
        }

        progressor.run(ctx, optSources.get(), 0);
    }

}
