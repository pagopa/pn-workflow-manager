package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InformalNotificationViewedDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.getInformalNotificationViewedTimelineElementId;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationViewedHandler {

    private static final String WEB_SOURCE_CHANNEL = "WEB";

    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final WorkflowUtils workflowUtils;
    private final NotificationService notificationService;
    private final CampaignService campaignService;
    private final AuditLogService auditLogService;

    public void handleViewNotification(NotificationViewedInt notificationViewed) {
        String iun = Objects.requireNonNull(notificationViewed.getIun(), "iun is required");
        Integer recIndex = Objects.requireNonNull(notificationViewed.getRecipientIndex(), "recipientIndex is required");

        String viewedTimelineElementId = getInformalNotificationViewedTimelineElementId(
                recIndex,
                iun,
                notificationViewed.getSourceChannel()
        );

        List<InformalNotificationViewedDetailsInt> existingRecipientViews =
                timelineService.getTimelineStrongly(iun, false).stream()
                        .filter(el -> el.getCategory() == TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED)
                        .map(TimelineElementInternal::getDetails)
                        .map(InformalNotificationViewedDetailsInt.class::cast)
                        .filter(d -> d.getRecIndex() == recIndex)
                        .toList();

        boolean alreadyViewedOnSameChannel = existingRecipientViews.stream()
                .anyMatch(d -> Objects.equals(d.getSourceChannel(), notificationViewed.getSourceChannel()));

        if (alreadyViewedOnSameChannel) {
            log.info("INFORMAL_NOTIFICATION_VIEWED already present for channel={}, skipping flow - iun={} recIndex={}",
                    notificationViewed.getSourceChannel(), iun, recIndex);
            return;
        }

        boolean isFirstViewForRecipient = existingRecipientViews.isEmpty();

        buildViewedAuditLogEvent(iun, recIndex);

        NotificationInt notification = notificationService.getInformalNotificationByIun(iun);
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(
                notification.getCampaignId(),
                notification.getSender().getPaId()
        );

        addInformalNotificationViewedTimelineElement(notificationViewed, notification, recIndex, viewedTimelineElementId,isFirstViewForRecipient);

        timelineUtils.handleTransitionToReachedStatusIfNecessary(notification, recIndex, viewedTimelineElementId);

        if (isWebViewWithStopOnViewed(notificationViewed, campaign)) {
            workflowUtils.scheduleWorkflowDone(iun, recIndex, viewedTimelineElementId, DesiredFeedbackType.READ);
        }
    }

    private void addInformalNotificationViewedTimelineElement(NotificationViewedInt notificationViewed,
                                                              NotificationInt notification,
                                                              Integer recIndex,
                                                              String viewedTimelineElementId,
                                                              boolean isFirstViewForRecipient) {
        TimelineElementInternal timelineElement = timelineUtils.buildInformalNotificationViewedTimelineElement(
                notification,
                recIndex,
                viewedTimelineElementId,
                notificationViewed.getViewedDate(),
                notificationViewed.getSourceChannel(),
                notificationViewed.getSourceChannelDetails(),
                isFirstViewForRecipient
        );
        timelineService.addTimelineElement(timelineElement, notification);
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


