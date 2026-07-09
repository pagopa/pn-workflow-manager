package it.pagopa.pn.workflowmanager.action.timeoutworkflow;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class TimeoutWorkflowActionHandler {
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final WorkflowUtils workflowUtils;

    public void timeoutWorkflowAction(String iun, int recIndex, TimeoutWorkflowDetails timeoutWorkflowDetails) {
        log.info("Timeout informal notification workflow for recipient - iun {} id {} channel {}",
                iun, recIndex, timeoutWorkflowDetails.getChannel());

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        NotificationRecipientInt currentRecipient = notificationInt.getRecipients().get(recIndex);

        log.debug("Retrieving campaign for campaignId {} - iun {}", notificationInt.getCampaignId(), iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notificationInt.getCampaignId(),
                notificationInt.getSender().getPaId()
        );

        workflowUtils.advanceWorkflow(iun, recIndex, timeoutWorkflowDetails.getChannel(), campaign, currentRecipient.getRecipientType());
    }
}
