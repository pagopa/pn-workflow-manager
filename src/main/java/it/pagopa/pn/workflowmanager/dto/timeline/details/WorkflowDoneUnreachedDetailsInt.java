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
public class WorkflowDoneUnreachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private String sourceElementId;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d sourceElementId=%s",
                recIndex,
                sourceElementId
        );
    }
}
