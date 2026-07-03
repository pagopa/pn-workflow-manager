package it.pagopa.pn.workflowmanager.action.utils;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.*;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
@Slf4j
public class TimelineUtils {
    private final TimelineService timelineService;

    public TimelineElementInternal buildWorkflowEndedUndeliverableTimelineElement(Integer recIndex, NotificationInt notification,
                                                                              String eventId) {
        log.debug("buildWorkflowEndedUndeliverableTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedUndeliverableDetailsInt details = WorkflowEndedUndeliverableDetailsInt.builder()
                .recIndex(recIndex)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_UNDELIVERABLE, eventId, details);
    }

    public static String getWorkflowEndedUndeliverableTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_UNDELIVERABLE.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowEndedUnreachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                            String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowEndedUnreachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedUnreachedDetailsInt details = WorkflowEndedUnreachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_UNREACHED, eventId, details);
    }

    public static String getWorkflowEndedUnreachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_UNREACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowEndedReachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                           String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowEndedReachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedReachedDetailsInt details = WorkflowEndedReachedDetailsInt.builder()
                .recIndex(recIndex)
                .notificationDate(Instant.now())
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_REACHED, eventId, details);
    }

    public static String getWorkflowEndedReachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_REACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowDoneUnreachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                              String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowDoneUnreachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowDoneUnreachedDetailsInt details = WorkflowDoneUnreachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED, eventId, details);
    }

    public static String getWorkflowDoneUnreachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_DONE_UNREACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowDoneReachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                            String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowDoneReachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowDoneReachedDetailsInt details = WorkflowDoneReachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_DONE_REACHED, eventId, details);
    }

    public static String getWorkflowDoneReachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_DONE_REACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 @NotNull TimelineElementDetailsInt details) {

        TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder = TimelineElementInternal.builder();

        return buildTimeline(notification, category, elementId, details, timelineBuilder);
    }

    private TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 TimelineElementDetailsInt details,
                                                 TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder) {
        return timelineBuilder
                .iun(notification.getIun())
                .category(category)
                .timestamp(Instant.now())
                .elementId(elementId)
                .details(details)
                .paId(notification.getSender().getPaId())
                .notificationSentAt(notification.getSentAt())
                .build();
    }

    public boolean checkTimelineCategories(List<TimelineElementInternal> timelineElements,
                                           int recIndex, TimelineElementCategoryInt... categories) {
        return hasAnyTimelineCategory(timelineElements, recIndex, categories);
    }

    public Stream<TimelineElementInternal> getTimelineElementInternals(String iun) {
        Set<TimelineElementInternal> timeline = timelineService.getTimeline(iun, false);
        return timeline.stream();
    }
    private boolean hasAnyTimelineCategory(List<TimelineElementInternal> timelineElements, int recIndex,
                                           TimelineElementCategoryInt... categories) {
        return Arrays.stream(categories)
                .anyMatch(category -> isTimelineElementPresent(timelineElements, recIndex, category));
    }

    private boolean isTimelineElementPresent(List<TimelineElementInternal> timelineElements, int recIndex,
                                             TimelineElementCategoryInt category) {
        return timelineElements.stream()
                .filter(element -> category.equals(element.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(details -> details instanceof RecipientRelatedTimelineElementDetails)
                .map(details -> (RecipientRelatedTimelineElementDetails) details)
                .anyMatch(details -> details.getRecIndex() == recIndex);
    }

}
