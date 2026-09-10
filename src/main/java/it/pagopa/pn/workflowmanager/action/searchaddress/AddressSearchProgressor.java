package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AsyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.SyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.GetAddressInfoDetailsInt;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AddressSearchProgressor {
    private final AddressSearchUtils utils;
    private final AddressSearchRegistry registry;

    public void run(AddressSearchContext ctx, List<DigitalAddressSourceInt> sources, int fromIndex) {
        for (int i = fromIndex; i < sources.size(); i++) {
            DigitalAddressSourceInt source = sources.get(i);

            Optional<GetAddressInfoDetailsInt> existing = utils.findPreviousSearchOutcome(
                    ctx.notification().getIun(),
                    ctx.recipientIndex(),
                    source,
                    ctx.channel(),
                    ctx.attempt()
            );
            if (existing.isPresent()) {
                if (existing.get().getIsAvailable()) {
                    utils.scheduleSendChannelMessageAction(ctx, source);
                    // Se sono qui è un ritentativo di ricerca, dunque provo solo a schedulare l'azione di invio, senza ripetere la ricerca per altre source.
                    return;
                }
                continue; // Ricerca già tentata, non trovata: idempotenza, passa alla prossima source
            }

            AddressSearchStrategy strategy = registry.find(source, ctx.channel());

            switch (strategy) {
                case SyncAddressSearchStrategy sync -> {
                    SourceSearchOutcome outcome = sync.search(ctx);
                    utils.storeSearchOutcome(ctx, outcome);
                    if (outcome.found()) {
                        utils.scheduleSendChannelMessageAction(ctx, source);
                        return;
                    }
                    // non trovato: continua il for
                }
                case AsyncAddressSearchStrategy async -> {
                    async.triggerSearch(ctx);
                    return; // sospende qui; ripresa dal callback con fromIndex = i + 1
                }
            }
        }

        // piano esaurito, nessun indirizzo trovato su nessuna fonte
        utils.scheduleSendChannelMessageAction(ctx, DigitalAddressSourceInt.NONE);
    }

}
