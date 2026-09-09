package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.AsyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.action.searchaddress.strategy.SyncAddressSearchStrategy;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AddressSearchProgressor {
    private final AddressSearchUtils utils;
    private final SchedulerService schedulerService;


    public void run(AddressSearchContext ctx, List<DigitalAddressSourceInt> sources, int fromIndex) {
        for (int i = fromIndex; i < sources.size(); i++) {
            DigitalAddressSourceInt source = sources.get(i);

            Optional<GetAddressInfoDetailsInt> existing = utils.findPreviousSearchOutcome(
                    ctx.iun(),
                    ctx.recipientIndex(),
                    source,
                    ctx.channel(),
                    ctx.attempt()
            );
            if (existing.isPresent()) {
                if (existing.get().isAvailable()) {
                    scheduleAction(ctx, source);
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
                        scheduleAction(ctx, source);
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
        scheduleAction(ctx, DigitalAddressSourceInt.NONE);
    }


}
