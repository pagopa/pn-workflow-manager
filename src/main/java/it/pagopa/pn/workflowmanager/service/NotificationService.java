package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface NotificationService {
    NotificationInt getInformalNotificationByIun(String iun);
}
