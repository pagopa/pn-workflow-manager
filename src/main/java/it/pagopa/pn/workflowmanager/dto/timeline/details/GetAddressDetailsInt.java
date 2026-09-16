package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class GetAddressDetailsInt extends CategoryTypeTimelineElementDetailsInt
        implements RecipientReachedTimelineElement, InformalDigitalAddressRelatedTimelineElement {
    private InformalDigitalAddressInt digitalAddress;
    private DigitalAddressSourceInt digitalAddressSource;
    private Integer attempt;
    private DigitalChannelsInt channel;
    private String category;
    private int recIndex;
    private Instant attemptDate;
    //Todo: mancano altri campi?

    @Override
    public String toLog() {
        return this.toString(); //TODO: ha info sensibili?
    }
}
