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
public class WorkflowDoneUnreachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, InvoiceableTimelineElement {
    private int recIndex;
    private String sourceElementId;
    private String completionFeedback;
    private Integer notificationCost;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d sourceElementId=%s completionFeedback=%s notificationCost=%d",
                recIndex,
                sourceElementId,
                completionFeedback,
                notificationCost
        );
    }
}
