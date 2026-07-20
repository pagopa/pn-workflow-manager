package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendDigitalMessageSkipDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, DigitalAddressSourceRelatedTimelineElement {
    private int recIndex;
    private DigitalChannelsInt channel;
    private DigitalAddressSourceInt digitalAddressSource;
    private Integer retryNumber;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d channel=%s digitalAddressSource=%s retryNumber=%s",
                recIndex,
                channel,
                digitalAddressSource,
                retryNumber
        );
    }
}
