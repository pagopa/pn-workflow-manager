package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.getInformalNotificationViewedTimelineElementId;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationViewedHandler {

    private static final String WEB_SOURCE_CHANNEL = "WEB";

    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final SchedulerService schedulerService;
    private final AuditLogService auditLogService;

    public void handleViewNotification(NotificationViewedInt notificationViewed) {
        String iun = Objects.requireNonNull(notificationViewed.getIun(), "iun is required");
        Integer recIndex = Objects.requireNonNull(notificationViewed.getRecipientIndex(), "recipientIndex is required");

        Optional<TimelineElementInternal> viewedTimelineElement = timelineService.getTimelineElementForSpecificRecipient(
                iun,
                recIndex,
                INFORMAL_NOTIFICATION_VIEWED
        );

        if (viewedTimelineElement.isPresent()) {
            log.info("INFORMAL_NOTIFICATION_VIEWED already present, skipping flow - iun={} recIndex={}", iun, recIndex);
            return;
        }

        buildViewedAuditLogEvent(iun, recIndex);

        NotificationInt notification = notificationService.getInformalNotificationByIun(iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notification.getCampaignId(),
                notification.getSender().getPaId()
        );

        String viewedTimelineElementId = getInformalNotificationViewedTimelineElementId(
                recIndex,
                iun,
                notificationViewed.getSourceChannel()
        );
        TimelineElementInternal timelineElement = timelineUtils.buildInformalNotificationViewedTimelineElement(
                notification,
                recIndex,
                viewedTimelineElementId,
                notificationViewed.getViewedDate(),
                notificationViewed.getSourceChannel(),
                notificationViewed.getSourceChannelDetails()
        );
        timelineService.addTimelineElement(timelineElement, notification);

        timelineUtils.handleTransitionToReachedStatusIfNecessary(notification, recIndex, viewedTimelineElementId);

        if (isWebViewWithStopOnViewed(notificationViewed, campaign)) {
            schedulerService.scheduleEvent(
                    iun,
                    recIndex,
                    Instant.now(),
                    ActionType.WORKFLOW_DONE,
                    viewedTimelineElementId,
                    new NotHandledDetails()
            );
        }
    }

    private void buildViewedAuditLogEvent(String iun, Integer recIndex) {
        String msg = "Notification viewed for recipient {}";
        auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_VIEW_RCP, msg, recIndex);
    }

    private boolean isWebViewWithStopOnViewed(NotificationViewedInt notificationViewed, Campaign campaign) {
        return WEB_SOURCE_CHANNEL.equals(notificationViewed.getSourceChannel())
                && Boolean.TRUE.equals(campaign.getStopOnViewed());
    }
}


