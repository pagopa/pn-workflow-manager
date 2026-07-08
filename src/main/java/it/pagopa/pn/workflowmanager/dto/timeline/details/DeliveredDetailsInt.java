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
public class DeliveredDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private String channel;
    private String sourceElementId;
    private Instant notificationDate;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d channel=%s sourceElementId=%s notificationDate=%s",
                recIndex,
                channel,
                sourceElementId,
                notificationDate
        );
    }
}
