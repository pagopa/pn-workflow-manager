package it.pagopa.pn.workflowmanager.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@SuperBuilder( toBuilder = true )
public abstract class DeliveryDetailsInt {
    private String code;
    private String failureCause;
    private Instant eventTimestamp;
}
