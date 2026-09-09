package it.pagopa.pn.workflowmanager.action.searchaddress.dto;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;

public record SourceSearchOutcome(
        DigitalAddressSourceInt source,
      boolean found,
      InformalDigitalAddressInt address,
      boolean tosAccepted
) {
    public static SourceSearchOutcome tosNotAccepted(DigitalAddressSourceInt source) {
        return new SourceSearchOutcome(source, false, null, false);
    }

    public static SourceSearchOutcome notFound(DigitalAddressSourceInt source) {
        return new SourceSearchOutcome(source, false, null, true);
    }

    public static SourceSearchOutcome found(DigitalAddressSourceInt source, InformalDigitalAddressInt address) {
        return new SourceSearchOutcome(source, true, address, true);
    }
}
