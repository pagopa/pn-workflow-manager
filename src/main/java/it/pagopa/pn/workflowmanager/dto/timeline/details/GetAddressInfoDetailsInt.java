package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
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
public class GetAddressInfoDetailsInt extends CategoryTypeTimelineElementDetailsInt implements DigitalAddressSourceRelatedTimelineElement, InformalDigitalAddressRelatedTimelineElement {
    private int recIndex;
    private DigitalAddressSourceInt digitalAddressSource;
    private Boolean isAvailable;
    private Instant attemptDate;
    private InformalDigitalAddressInt digitalAddress;
    private Boolean isTosAccepted;
    private DigitalChannelsInt channel;

    public String toLog() {
        return String.format(
                "recIndex=%d digitalAddressSource=%s isAvailable=%s",
                recIndex,
                digitalAddressSource,
                isAvailable
        );
    }
}
