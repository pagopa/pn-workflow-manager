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
public class WorkflowEndedReachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private Instant notificationDate;
    private String sourceElementId;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d notificationDate=%s sourceElementId=%s",
                recIndex,
                notificationDate,
                sourceElementId
        );
    }
}
