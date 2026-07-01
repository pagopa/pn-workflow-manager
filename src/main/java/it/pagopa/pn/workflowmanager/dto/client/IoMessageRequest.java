package it.pagopa.pn.workflowmanager.dto.client;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode
@AllArgsConstructor
public class IoMessageRequest {
    private String requestId;
    private NotificationInt notificationInt;
    private NotificationRecipientInt notificationRecipientInt;
    private Campaign campaign;
    private String markdown;
}
