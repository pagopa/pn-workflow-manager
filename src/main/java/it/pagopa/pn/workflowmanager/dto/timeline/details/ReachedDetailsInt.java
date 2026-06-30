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
public class ReachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private String channel;
    private String sourceElementId;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d channel=%s sourceElementId=%s",
                recIndex,
                channel,
                sourceElementId
        );
    }
}
