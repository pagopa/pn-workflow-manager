package it.pagopa.pn.workflowmanager.dto.notification.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.datavault.RecipientTypeInt;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotificationRecipientInt {
    private String taxId;
    private String internalId;
    private String denomination;
    private InformalDigitalAddressInt informalDigitalAddressInt;
    private PhysicalAddressInt physicalAddress;
    private List<NotificationPaymentInfoInt> payments;
    private RecipientTypeInt recipientType;
}
