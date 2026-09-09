package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;

public non-sealed interface SyncAddressSearchStrategy extends AddressSearchStrategy {
    SourceSearchOutcome search(AddressSearchContext context);
}
