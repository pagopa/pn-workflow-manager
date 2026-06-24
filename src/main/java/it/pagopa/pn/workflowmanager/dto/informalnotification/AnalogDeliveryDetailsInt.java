package it.pagopa.pn.workflowmanager.dto.informalnotification;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class AnalogDeliveryDetailsInt extends DeliveryDetailsInt {
}
