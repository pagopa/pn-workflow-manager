package it.pagopa.pn.workflowmanager.action;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;

public interface ChannelSender {
    ChannelType getChannelType();
    void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep);
}
