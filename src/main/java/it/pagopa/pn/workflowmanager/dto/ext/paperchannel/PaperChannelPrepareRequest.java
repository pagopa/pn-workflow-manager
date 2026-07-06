package it.pagopa.pn.workflowmanager.dto.ext.paperchannel;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode
@AllArgsConstructor
public class PaperChannelPrepareRequest {
    private final NotificationInt notificationInt;
    private final NotificationRecipientInt recipientInt;
    private final PhysicalAddressInt paAddress;
    private final String requestId;
    private final PhysicalAddressInt.ANALOG_TYPE analogType;
    private final List<String> attachments;
    private final String relatedRequestId;
}
