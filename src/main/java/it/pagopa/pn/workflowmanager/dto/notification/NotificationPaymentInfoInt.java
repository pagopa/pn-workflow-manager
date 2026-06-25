package it.pagopa.pn.workflowmanager.dto.notification;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationPaymentInfoInt {
    private PagoPaInt pagoPA;
}
