package it.pagopa.pn.workflowmanager.dto.notification;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationSenderInt {
    private String paId;
    private String paDenomination;
    private String paTaxId;
    private Integer physicalCommunicationPriority;
}
