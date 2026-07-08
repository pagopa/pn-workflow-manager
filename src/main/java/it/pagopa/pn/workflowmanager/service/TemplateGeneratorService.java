package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

import java.io.File;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;

public interface TemplateGeneratorService {
    String generateIoMessageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign);
    String generatePecBodyTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign);
    String generatePecSubjectTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt);
    String generateEmailBodyTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign);
    String generateEmailSubjectTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt);
    File generateCoverpageTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt, Campaign campaign);
    String generateSmsTemplate(NotificationInt notificationInt, NotificationRecipientInt notificationRecipientInt);
}
