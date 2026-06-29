package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;


import it.pagopa.pn.workflowmanager.dto.timeline.details.common.CategoryTypeTimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.RecipientRelatedTimelineElementDetails;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class WorkflowEndedReachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private Instant notificationDate;
    private List<String> channels;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d notificationDate=%s channels=%s",
                recIndex,
                notificationDate,
                channels
        );
    }
}
