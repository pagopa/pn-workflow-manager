package it.pagopa.pn.workflowmanager.action.searchaddress.dto;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;

public record SourceSearchOutcome(
      DigitalAddressSourceInt source,
      boolean found,
      InformalDigitalAddressInt address,
      Boolean tosAccepted
) {
    public static SourceSearchOutcome tosNotAccepted(DigitalAddressSourceInt source) {
        return new SourceSearchOutcome(source, false, null, false);
    }

    public static SourceSearchOutcome notFound(DigitalAddressSourceInt source) {
        return notFound(source, null);
    }

    public static SourceSearchOutcome notFound(DigitalAddressSourceInt source, Boolean tosAccepted) {
        return new SourceSearchOutcome(source, false, null, tosAccepted);
    }

    public static SourceSearchOutcome found(DigitalAddressSourceInt source, InformalDigitalAddressInt address) {
        return found(source, address, null);
    }

    public static SourceSearchOutcome found(DigitalAddressSourceInt source, InformalDigitalAddressInt address, Boolean tosAccepted) {
        return new SourceSearchOutcome(source, true, address, tosAccepted);
    }
}
