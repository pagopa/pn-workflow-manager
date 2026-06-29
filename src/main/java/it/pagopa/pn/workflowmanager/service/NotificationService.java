package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.notification.common.NotificationInt;

public interface NotificationService {
    NotificationInt getInformalNotificationByIun(String iun);
}
