package it.pagopa.pn.workflowmanager.dto.timeline;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementDetailsInt;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimelineElementInternal implements Comparable<TimelineElementInternal> {
    private String iun;
    private String elementId;
    private Instant timestamp;
    private String paId;
    private TimelineElementCategoryInt category;
    private TimelineElementDetailsInt details;
    private StatusInfoInternal statusInfo;
    private Instant notificationSentAt;
    private Instant ingestionTimestamp; //Questo campo viene valorizzato solo ed esclusivamente in uscita per api e webhook dal mapper
    private Instant eventTimestamp; //Questo campo viene valorizzato solo ed esclusivamente in uscita per api e webhook dal mapper
    private CommunicationType communicationType;
    private String campaignId;

    @Override
    public int compareTo(@NotNull TimelineElementInternal o) {
        int order = this.timestamp.compareTo(o.getTimestamp());
        if (order == 0)
            order = this.category.getPriority() - o.getCategory().getPriority();
        if (order == 0)
            order = this.elementId.compareTo(o.getElementId());
        return order;
    }
}
