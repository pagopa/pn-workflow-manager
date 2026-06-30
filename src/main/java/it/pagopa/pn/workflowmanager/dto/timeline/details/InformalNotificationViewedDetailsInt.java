package it.pagopa.pn.workflowmanager.dto.timeline.details;

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
public class InformalNotificationViewedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private Instant eventTimestamp;
    private String sourceChannel;
    private String sourceChannelDetails;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d sourceChannel=%s sourceChannelDetails=%s eventTimestamp=%s",
                recIndex,
                sourceChannel,
                sourceChannelDetails,
                eventTimestamp
        );
    }
}
