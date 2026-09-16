package it.pagopa.pn.workflowmanager.action.startworkflow;


import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface ChannelSender {
    ChannelType getChannelType();
    void send(NotificationInt notification, Campaign campaign, int recIndex, DigitalAddressSourceInt addressSource);
}
