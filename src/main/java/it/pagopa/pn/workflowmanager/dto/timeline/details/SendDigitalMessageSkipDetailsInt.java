package it.pagopa.pn.workflowmanager.dto.timeline.details;


import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendDigitalMessageSkipDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private DigitalChannelsInt channel;
    private Integer retryNumber;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d channel=%s retryNumber=%s",
                recIndex,
                channel,
                retryNumber
        );
    }
}
