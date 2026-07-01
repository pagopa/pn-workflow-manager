package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.ChannelSenderFactory;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StartWorkflowActionHandler {
    private final ChannelSenderFactory channelSenderFactory;
    private final NotificationService notificationService;
    private final CampaignService campaignService;

    public void startWorkflowAction(String iun, int recIndex, StartWorkflowDetails startWorkflowDetails) {
        log.info("Start informal notification workflow for recipient - iun {} id {} channel {}",
                iun, recIndex, startWorkflowDetails.getChannel());

        log.debug("Getting channel sender for channel {} - iun {}", startWorkflowDetails.getChannel(), iun);
        ChannelSender channelSender = channelSenderFactory.getChannelSender(startWorkflowDetails.getChannel());

        log.debug("Retrieving notification for iun {}", iun);
        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        log.debug("Retrieving campaign for campaignId {} senderId {} - iun {}",
                notificationInt.getCampaignId(), notificationInt.getSender().getPaId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId()
        );

        log.info("Sending notification via channel {} for iun {} recipient {} campaignId {}",
                startWorkflowDetails.getChannel(), iun, recIndex, campaign.getCampaignId());
        channelSender.send(notificationInt, campaign, recIndex, 0, startWorkflowDetails.getChannel());

        log.info("Workflow started successfully for iun {} recipient {}", iun, recIndex);
    }
}
