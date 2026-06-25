package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;


import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.CategoryTypeTimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.DigitalAddressSourceRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.RecipientRelatedTimelineElementDetails;
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
