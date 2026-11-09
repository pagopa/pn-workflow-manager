package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;

import java.util.List;

public interface WorkflowClosureTimelineService {
    void saveWorkflowDoneReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements);

    void saveWorkflowDoneUnreachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, DesiredFeedbackType completionFeedback, List<TimelineElementInternal> timelineElements);

    void saveWorkflowEndedReachedTimelineElement(int recIndex, String sourceTimelineId, NotificationInt notification, List<TimelineElementInternal> timelineElements);

    void saveWorkflowEndedUnreachedTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements);

    void saveWorkflowEndedUndeliverableTimelineElement(int recIndex, NotificationInt notification, List<TimelineElementInternal> timelineElements);

    void handleTransitionToReachedStatusIfNecessary(NotificationInt notificationInt, int recIndex, String sourceTimelineId);
}
