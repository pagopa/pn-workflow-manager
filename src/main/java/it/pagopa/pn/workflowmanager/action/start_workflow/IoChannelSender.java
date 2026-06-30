package it.pagopa.pn.workflowmanager.action.start_workflow;

import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector.IoConnectorClient;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class IoChannelSender { //TODO: Implementa interfaccia ChannelSender
    private final TemplateGeneratorService templateGeneratorService;
    private final IoConnectorClient ioConnectorClient;
    private final ChannelSenderUtils channelSenderUtils;
    private final WorkflowUtils workflowUtils;

    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep, ChannelType channel) {
        log.info("Sending message for notification {} to recipient {} via channel {}", notification.getIun(), recIndex, channel);
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        // TODO: capire se isIoUser serve.
        String markdown = templateGeneratorService.generateIoMessageTemplate(notification, recipient, false);
        String requestId = ChannelSenderUtils.buildSendDigitalMessageEventId(notification.getIun(), recIndex, channel);
        ioConnectorClient.sendMessage(
            IoMessageRequest.builder()
                .requestId(requestId)
                .markdown(markdown)
                .notificationInt(notification)
                .notificationRecipientInt(recipient)
                .campaign(campaign)
                .build()
        );

        channelSenderUtils.saveSendDigitalMessageElement(
                notification,
                requestId,
                recIndex,
                ChannelSenderUtils.buildDigitalAddress(recipient.getTaxId(), InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.APPIO),
                DigitalChannelsInt.APPIO,
                null
        );

        workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, currentStep, campaign, channel);
    }
}
