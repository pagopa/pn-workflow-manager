package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationHistoryResponseInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.service.WorkflowClosureTimelineService;
import it.pagopa.pn.workflowmanager.utils.NotificationCostUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowClosureTimelineServiceIml implements WorkflowClosureTimelineService {
    private final NotificationCostUtils notificationCostUtils;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;

    @Override
    public void saveWorkflowDoneReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow done reached timeline element for iun {}", notification.getIun());
        Integer notificationCost = notificationCostUtils.getNotificationCostToApply(timelineElements,recIndex);
        addTimelineElement(timelineUtils.buildWorkflowDoneReachedTimelineElement(recIndex, notification,
                getWorkflowDoneReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, completionFeedback, notificationCost),notification);
    }

    @Override
    public void saveWorkflowDoneUnreachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow done unreached timeline element for iun {}", notification.getIun());
        Integer cost = notificationCostUtils.getNotificationCostToApply(timelineElements,recIndex);
        addTimelineElement(timelineUtils.buildWorkflowDoneUnreachedTimelineElement(recIndex, notification,
                getWorkflowDoneUnreachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, completionFeedback, cost), notification);
    }

    @Override
    public void saveWorkflowEndedReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended reached timeline element for iun {}", notification.getIun());
        Integer cost = notificationCostUtils.getNotificationCostToApply(timelineElements,recIndex);
        addTimelineElement(timelineUtils.buildWorkflowEndedReachedTimelineElement(recIndex, notification,
                getWorkflowEndedReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, cost), notification);
    }

    @Override
    public void saveWorkflowEndedUnreachedTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended unreached timeline element for iun {}", notification.getIun());
        Integer cost = notificationCostUtils.getNotificationCostToApply(timelineElements,recIndex);
        addTimelineElement(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(recIndex, notification,
                getWorkflowEndedUnreachedTimelineElementId(recIndex, notification.getIun()), cost), notification);
    }

    @Override
    public void saveWorkflowEndedUndeliverableTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended undeliverable timeline element for iun {}", notification.getIun());
        Integer cost = notificationCostUtils.getNotificationCostToApply(timelineElements,recIndex);
        addTimelineElement(timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(recIndex, notification,
                getWorkflowEndedUndeliverableTimelineElementId(recIndex, notification.getIun()), cost), notification);
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        log.info("Creating timeline element with category {} for iun {}", element.getCategory(), element.getIun());
        timelineService.addTimelineElement(element, notification);
    }

    @Override
    public void handleTransitionToReachedStatusIfNecessary(NotificationInt notificationInt, int recIndex, String sourceTimelineId) {
        String iun = notificationInt.getIun();
        int numOfRecipients = notificationInt.getRecipients().size();
        Instant sentAt = notificationInt.getSentAt();
        NotificationHistoryResponseInt history = timelineService.getTimelineAndStatusHistory(iun, numOfRecipients, sentAt);
        NotificationStatusInt currentStatus = history.getNotificationStatus();
        if (currentStatus == NotificationStatusInt.COMPLETED_UNREACHED || currentStatus == NotificationStatusInt.UNDELIVERABLE) {
            log.info("Notification {} is in COMPLETED_UNREACHED status, saving element with category WORKFLOW_ENDED_REACHED for recIndex {}", iun, recIndex);
            saveWorkflowEndedReachedTimelineElement(recIndex,
                    sourceTimelineId,
                    notificationInt,
                    history.getTimeline()
            );
        }
    }

}
