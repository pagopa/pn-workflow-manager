package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;

public non-sealed interface AsyncAddressSearchStrategy extends AddressSearchStrategy {
    /** Avvia la ricerca; la risposta arriva su un canale separato (callback/evento) */
    void triggerSearch(AddressSearchContext context);
}
