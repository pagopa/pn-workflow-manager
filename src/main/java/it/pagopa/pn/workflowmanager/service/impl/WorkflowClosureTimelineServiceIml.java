package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.service.WorkflowClosureTimelineService;
import it.pagopa.pn.workflowmanager.utils.WorkflowClosureTimelineUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowClosureTimelineServiceIml implements WorkflowClosureTimelineService {
    private final WorkflowClosureTimelineUtils workflowClosureTimelineUtils;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;

    @Override
    public void saveWorkflowDoneReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow done reached timeline element for iun {}", notification.getIun());
        Integer notificationCost = workflowClosureTimelineUtils.getNotificationCostToApply(timelineElements);
        addTimelineElement(timelineUtils.buildWorkflowDoneReachedTimelineElement(recIndex, notification,
                getWorkflowDoneReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, completionFeedback, notificationCost),notification);
    }

    @Override
    public void saveWorkflowDoneUnreachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow done unreached timeline element for iun {}", notification.getIun());
        Integer cost = workflowClosureTimelineUtils.getNotificationCostToApply(timelineElements);
        addTimelineElement(timelineUtils.buildWorkflowDoneUnreachedTimelineElement(recIndex, notification,
                getWorkflowDoneUnreachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, completionFeedback, cost), notification);
    }

    @Override
    public void saveWorkflowEndedReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended reached timeline element for iun {}", notification.getIun());
        Integer cost = workflowClosureTimelineUtils.getNotificationCostToApply(timelineElements);
        addTimelineElement(timelineUtils.buildWorkflowEndedReachedTimelineElement(recIndex, notification,
                getWorkflowEndedReachedTimelineElementId(recIndex, notification.getIun()), sourceTimelineId, cost), notification);
    }

    @Override
    public void saveWorkflowEndedUnreachedTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended unreached timeline element for iun {}", notification.getIun());
        Integer cost = workflowClosureTimelineUtils.getNotificationCostToApply(timelineElements);
        addTimelineElement(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(recIndex, notification,
                getWorkflowEndedUnreachedTimelineElementId(recIndex, notification.getIun()), cost), notification);
    }

    @Override
    public void saveWorkflowEndedUndeliverableTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements) {
        log.info("Saving workflow ended undeliverable timeline element for iun {}", notification.getIun());
        Integer cost = workflowClosureTimelineUtils.getNotificationCostToApply(timelineElements);
        addTimelineElement(timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(recIndex, notification,
                getWorkflowEndedUndeliverableTimelineElementId(recIndex, notification.getIun()), cost), notification);
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        log.info("Creating timeline element with category {} for iun {}", element.getCategory(), element.getIun());
        timelineService.addTimelineElement(element, notification);
    }

}
