package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;

import it.pagopa.pn.workflowmanager.dto.timeline.details.common.CategoryTypeTimelineElementDetailsInt;
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
public class WorkflowEndedUndeliverableDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;

    @Override
    public String toLog() {
        return String.format("recIndex=%d", recIndex);
    }
}
