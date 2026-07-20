package it.pagopa.pn.workflowmanager.dto.timeline.details;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationRefusedErrorInt {
    private String errorCode;
    private String detail;
    private Integer recIndex;
}
