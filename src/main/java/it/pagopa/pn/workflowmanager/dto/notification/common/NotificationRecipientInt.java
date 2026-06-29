package it.pagopa.pn.workflowmanager.dto.notification.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.pagopa.pn.commons.utils.qr.models.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
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
    private String email;
    private String phoneNumber;
    private String messageId;
    private NotificationMessageInt message;
    private List<String> additionalLanguages;
}
