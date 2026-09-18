package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddressSearchOrchestrator {
    private final ChannelAddressSourceConfigResolver configResolver;
    private final AddressSearchProgressor progressor;
    private final AddressSearchUtils searchUtils;

    /**
     * Avvia l'orchestrazione della ricerca di indirizzi digitali per un destinatario
     * @param ctx Dati contestuali della ricerca di indirizzo digitale
     */
    public void start(AddressSearchContext ctx) {
        if(ChannelType.IO.equals(ctx.channel()) || ChannelType.ANALOG.equals(ctx.channel())) {
            // Se non ci sono sorgenti configurate per il canale, schedulo direttamente l'azione di invio con source SPECIAL.
            searchUtils.scheduleSendChannelMessageAction(ctx, DigitalAddressSourceInt.SPECIAL);
            return;
        }

        List<DigitalAddressSourceInt> sources = getSourcesForChannel(ctx.channel(), ctx);
        progressor.run(ctx, sources, 0);
    }

    /**
     * Riprende l'orchestrazione della ricerca di indirizzi digitali in seguito all'esito di una ricerca su sorgente asincrona
     * @param ctx Dati contestuali della ricerca di indirizzo digitale
     * @param asyncSource Sorgente asincrona che ha restituito l'esito
     * @param outcome Esito della ricerca asincrona
     */
    public void resume(AddressSearchContext ctx, DigitalAddressSourceInt asyncSource, SourceSearchOutcome outcome) {
        List<DigitalAddressSourceInt> sources = getSourcesForChannel(ctx.channel(), ctx);
        progressor.resumeAfterAsyncOutcome(ctx, asyncSource, outcome, sources);
    }

    private List<DigitalAddressSourceInt> getSourcesForChannel(ChannelType channel, AddressSearchContext ctx) {
        return configResolver.resolveSources(channel, ctx.sentAt())
                .orElse(List.of(DigitalAddressSourceInt.SPECIAL)); // In assenza di sorgenti configurate per canale, effettuiamo di default una ricerca SPECIAL
    }
}
