package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelOutcomeHandler;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.io.IoEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class IoEventHandler {
    private final TimelineUtils timelineUtils;
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final IoEventNormalizer ioEventNormalizer;
    private final ChannelOutcomeHandler channelOutcomeHandler;

    public void handle(IoOutcomeEvent event) {
        log.info("Handling IO outcome event: {}", event);

        String iun = timelineUtils.getIunFromTimelineId(event.getRequestId());
        int recIndex = timelineUtils.checkIfSendRequestIsPresentAndRetrieveRecIndex(iun, event.getRequestId());
        MdcUtils.addIunAndCorrIdToMdc(iun, event.getRequestId());

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(notificationInt.getCampaignId(), notificationInt.getSender().getPaId());

        NormalizedChannelOutcome normalizedChannelOutcome = ioEventNormalizer.normalize(event, notificationInt, recIndex);
        log.debug("Normalized channel outcome: {}", normalizedChannelOutcome);
        channelOutcomeHandler.handleOutcome(normalizedChannelOutcome, notificationInt, campaign);
    }
}
