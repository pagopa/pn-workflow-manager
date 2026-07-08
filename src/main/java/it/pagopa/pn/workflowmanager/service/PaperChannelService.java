package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface PaperChannelService {
    void prepareSimpleRegisteredLetter(NotificationInt notification, Integer recIndex, String coverpageFileKey);
}
