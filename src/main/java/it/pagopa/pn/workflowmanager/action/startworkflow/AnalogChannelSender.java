package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.SaveDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.action.utils.NotificationUtils.getRecipientFromIndex;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalogChannelSender implements ChannelSender {
    private final SaveDocumentService saveDocumentService;
    private final ChannelSenderUtils channelSenderUtils;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.ANALOG;
    }

    @Override
    public void send(NotificationInt notification, Campaign campaign, int recIndex, int currentStep) {
        log.info("AnalogChannelSender send - iun={} recIndex={} currentStep={}", notification.getIun(), recIndex, currentStep);
        NotificationRecipientInt recipient = getRecipientFromIndex(notification, recIndex);
        try {
            String timelineEventId = TimelineUtils.buildCoverpageCreationTimelineEventId(notification.getIun(), recIndex);
            String fileKey = saveDocumentService.saveCoverpage(notification, recipient, campaign, timelineEventId, recIndex);
            log.info("Coverpage saved for iun={} recIndex={} fileKey={}", notification.getIun(), recIndex, fileKey);
            channelSenderUtils.saveCoverpageCreationElement(notification, recIndex, fileKey);
        } catch (Exception e) {
            throw new PnInternalException(
                    "Error sending analog notification " + notification.getIun() + " to recipientIdx " + recIndex,
                    ERROR_CODE_WORKFLOWMANAGER_SEND_ON_CHANNEL_ERROR, e);
        }
    }
}
