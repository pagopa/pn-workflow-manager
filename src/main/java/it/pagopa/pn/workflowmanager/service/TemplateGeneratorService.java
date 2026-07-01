package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

import java.io.File;

public interface TemplateGeneratorService {
    String generateIoMessageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, boolean isIoUser);
    File informalAnalogCommunication(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, boolean isIoUser);

}
