package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class ChannelEventProcessor {
    private final TimelineUtils timelineUtils;
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final ChannelOutcomeHandler channelOutcomeHandler;

    public <E extends ChannelOutcomeEvent> void process(E event, ChannelOutcomeNormalizer<E> normalizer) {
        log.info("Start process outcome event: {}", event);

        String iun = timelineUtils.getIunFromTimelineId(event.getRequestId());
        SendRelatedTimelineElement sourceSendRequestDetails = timelineUtils.checkAndRetrieveSourceSendRequestDetails(iun, event.getRequestId());
        MdcUtils.addIunAndCorrIdToMdc(iun, event.getRequestId());

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(notificationInt.getCampaignId(), notificationInt.getSender().getPaId());

        NormalizedChannelOutcome normalizedChannelOutcome = normalizer.normalize(event, notificationInt, sourceSendRequestDetails);
        log.debug("Normalized channel outcome: {}", normalizedChannelOutcome);
        channelOutcomeHandler.handleOutcome(normalizedChannelOutcome, notificationInt, campaign);
    }
}
