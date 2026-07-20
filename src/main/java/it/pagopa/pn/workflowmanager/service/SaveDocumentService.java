package it.pagopa.pn.workflowmanager.service;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;

public interface SaveDocumentService {
    String saveCoverpage(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            Campaign campaign,
            String timelineElementId,
            int recIndex
    );
}
