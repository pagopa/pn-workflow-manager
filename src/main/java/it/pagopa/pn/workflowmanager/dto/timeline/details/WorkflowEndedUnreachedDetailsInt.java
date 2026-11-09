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
public class WorkflowEndedUnreachedDetailsInt extends CategoryTypeTimelineElementDetailsInt implements InvoiceableTimelineElement {
    private int recIndex;
    private Integer notificationCost;

    @Override
    public String toLog() {
        return String.format("recIndex=%d notificationCost=%d", recIndex, notificationCost);
    }
}
