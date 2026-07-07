package it.pagopa.pn.workflowmanager.action.start_workflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.AttachmentUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND;

@Component
@RequiredArgsConstructor
@CustomLog
public class PecChannelSender implements ChannelSender {

    private final TemplateGeneratorService templateGeneratorService;
    private final AttachmentUtils attachmentUtils;
    private final PnExternalChannelsClient pnExternalChannelsClient;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep, ChannelType channel) {
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        WorkFlowEntity workflowStep = resolveWorkflowStep(campaign, recipient, currentStep, channel);

        String messageText = templateGeneratorService.generatePecBodyTemplate(notification, recipient, campaign);
        String subject = templateGeneratorService.generatePecSubjectTemplate(notification, recipient);

        String timelineId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, channel);

        pnExternalChannelsClient.sendNotificationPEC(
                timelineId,
                messageText,
                subject,
                notification,
                recipient,
                recipient.getDigitalDomicile(),
                resolveAttachmentUrls(notification, workflowStep, recIndex, channel)
        );

        channelSenderUtils.saveSendDigitalMessageElement(
                notification,
                timelineId,
                recIndex,
                ChannelSenderUtils.buildDigitalAddress(recipient.getEmail(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC),
                DigitalChannelsInt.PEC,
                null
        );

        workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, currentStep, campaign, channel);
    }

    private List<String> resolveAttachmentUrls(
            NotificationInt notification,
            WorkFlowEntity workflowStep,
            int recIndex,
            ChannelType channel
    ) {
        if (!Boolean.TRUE.equals(workflowStep.getIncludeAttachment())) {
            return List.of();
        }

        // Uses the configured attachment mode and formats URLs without docTag.
        return attachmentUtils.retrieveAttachments(
                notification,
                recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, channel),
                false
        );
    }

    private WorkFlowEntity resolveWorkflowStep(Campaign campaign, NotificationRecipientInt recipient, int currentStep, ChannelType channel) {
        List<WorkFlowEntity> workflowSteps = campaign.getWorkflow() == null
                ? List.of()
                : campaign.getWorkflow().stream()
                .filter(step -> step.getRecipientType() != null && step.getRecipientType().contains(recipient.getRecipientType()))
                .toList();

        if (workflowSteps.isEmpty() || currentStep < 0 || currentStep >= workflowSteps.size()) {
            String message = String.format("Step %d not configured for campaign=%s", currentStep, campaign.getCampaignId());
            throw new PnInternalException(message, ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND);
        }

        WorkFlowEntity workflowStep = workflowSteps.get(currentStep);
        boolean channelCompatible = workflowStep.getChannel() == channel;

        if (!channelCompatible) {
            String message = String.format(
                    "Step %d not compatible for channel=%s campaign=%s",
                    currentStep,
                    channel,
                    campaign.getCampaignId()
            );
            throw new PnInternalException(message, ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND);
        }

        return workflowStep;
    }
}

