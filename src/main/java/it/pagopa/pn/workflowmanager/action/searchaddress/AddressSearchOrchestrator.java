package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
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

        if(optSources.isEmpty()) {
            // Se non ci sono sorgenti configurate per il canale, schedulo direttamente l'azione di invio con source SPECIAL.
            searchUtils.scheduleSendChannelMessageAction(ctx, DigitalAddressSourceInt.SPECIAL);
            return;
        }

        progressor.run(ctx, optSources.get(), 0);
    }

}
