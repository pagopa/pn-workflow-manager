package it.pagopa.pn.workflowmanager.action.searchaddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDigitalDomicileConfig {
    private Instant validFrom;
    private List<DigitalAddressSourceInt> pec;
    private List<DigitalAddressSourceInt> sms;
    private List<DigitalAddressSourceInt> email;
}
