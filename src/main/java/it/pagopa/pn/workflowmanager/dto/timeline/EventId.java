package it.pagopa.pn.workflowmanager.dto.timeline;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class EventId {
    private String iun;
    private Integer recIndex;
    private Integer sentAttemptMade;
    private Integer progressIndex;
    private String deliveryType;
    private String channel;
}
