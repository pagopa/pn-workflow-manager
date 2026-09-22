package it.pagopa.pn.workflowmanager.action.sendchannelmessage;

import it.pagopa.pn.workflowmanager.action.startworkflow.ChannelSender;
import it.pagopa.pn.workflowmanager.action.startworkflow.ChannelSenderFactory;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendChannelMessageDetails;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class SendChannelMessageActionHandler {
    private final ChannelSenderFactory channelSenderFactory;
    private final NotificationService notificationService;
    private final CampaignService campaignService;

    public void sendChannelMessageAction(String iun, int recIndex, SendChannelMessageDetails details) {
        log.info("Send channel message for recipient - iun {} id {} channel {}", iun, recIndex, details.getChannel());

        ChannelSender channelSender = channelSenderFactory.getChannelSender(details.getChannel());
        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId()
        );

        log.info("Sending notification via channel {} for iun {} recipient {} campaignId {}",
                details.getChannel(), iun, recIndex, campaign.getCampaignId());
        channelSender.send(notificationInt, campaign, recIndex, details.getAddressSource());
    }
}
