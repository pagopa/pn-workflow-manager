package it.pagopa.pn.workflowmanager.action.postacceptedprocessing;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class PostAcceptedProcessingHandler {

    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final WorkflowUtils workflowUtils;

    public void handle(String iun) {
        NotificationInt notification = notificationService.getInformalNotificationByIun(iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notification.getCampaignId(),
                notification.getSender().getPaId()
        );

        List<NotificationRecipientInt> recipients = notification.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            throw new PnWorkflowException(String.format("No recipients found for iun %s", iun));
        }

        for (int recIndex = 0; recIndex < recipients.size(); recIndex++) {
            RecipientTypeInt recipientType = recipients.get(recIndex).getRecipientType();
            ChannelType firstChannel = getFirstChannelForRecipientType(campaign, recipientType);

            workflowUtils.scheduleStartWorkflow(iun, recIndex, 0, firstChannel);
        }
    }

    private ChannelType getFirstChannelForRecipientType(Campaign campaign, RecipientTypeInt recipientType) {
        return campaign.getWorkflowsByRecipientType(recipientType).getFirst().getChannel();
    }
}

