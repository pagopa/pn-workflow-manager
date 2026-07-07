package it.pagopa.pn.workflowmanager.service;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

public interface SaveDocumentService {
    String saveCoverpage(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            String timelineElementId,
            String recIndex
    );
}
