package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

import java.time.Instant;

public record AddressSearchContext(ChannelType channel, Instant sentAt, NotificationInt notification, Integer recipientIndex, int attempt) {
}
