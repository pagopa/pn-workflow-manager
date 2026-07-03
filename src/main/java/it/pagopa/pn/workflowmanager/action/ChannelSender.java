package it.pagopa.pn.workflowmanager.action;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;

public interface ChannelSender {
    void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep, ChannelType channel);
}
