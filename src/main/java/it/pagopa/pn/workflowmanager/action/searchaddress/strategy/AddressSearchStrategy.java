package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;

import java.util.Set;

public sealed interface AddressSearchStrategy permits SyncAddressSearchStrategy, AsyncAddressSearchStrategy {
    Set<SourceChannelKey> supportedKeys();
}
