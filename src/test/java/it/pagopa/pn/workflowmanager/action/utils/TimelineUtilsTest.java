package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.*;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineUtilsTest {

    @Mock
    private TimelineService timelineService;

    private TimelineUtils timelineUtils;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_PA_ID = "PA-001";
    private static final String TEST_EVENT_ID = "EVENT-001";
    private static final String TEST_SOURCE_TIMELINE_ID = "SOURCE-TIMELINE-001";

    @BeforeEach
    void setup() {
        timelineUtils = new TimelineUtils(timelineService);
    }

    @Test
    void buildWorkflowEndedUndeliverableTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_ENDED_UNDELIVERABLE, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowEndedUndeliverableDetailsInt.class, actual.getDetails())
        );

        WorkflowEndedUndeliverableDetailsInt details = (WorkflowEndedUndeliverableDetailsInt) actual.getDetails();
        Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex());
    }

    @Test
    void getWorkflowEndedUndeliverableTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowEndedUndeliverableTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertTrue(result.contains(TEST_IUN))
        );
    }

    @Test
    void buildWorkflowEndedUnreachedTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowEndedUnreachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_ENDED_UNREACHED, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowEndedUnreachedDetailsInt.class, actual.getDetails())
        );

        WorkflowEndedUnreachedDetailsInt details = (WorkflowEndedUnreachedDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId())
        );
    }

    @Test
    void getWorkflowEndedUnreachedTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowEndedUnreachedTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertTrue(result.contains(TEST_IUN))
        );
    }

    @Test
    void buildWorkflowEndedReachedTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowEndedReachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_ENDED_REACHED, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowEndedReachedDetailsInt.class, actual.getDetails())
        );

        WorkflowEndedReachedDetailsInt details = (WorkflowEndedReachedDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId()),
                () -> Assertions.assertNotNull(details.getNotificationDate())
        );
    }

    @Test
    void getWorkflowEndedReachedTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowEndedReachedTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertTrue(result.contains(TEST_IUN))
        );
    }

    @Test
    void buildWorkflowDoneUnreachedTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowDoneUnreachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_DONE_UNREACHED, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowDoneUnreachedDetailsInt.class, actual.getDetails())
        );

        WorkflowDoneUnreachedDetailsInt details = (WorkflowDoneUnreachedDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId())
        );
    }

    @Test
    void getWorkflowDoneUnreachedTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowDoneUnreachedTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertTrue(result.contains(TEST_IUN))
        );
    }

    @Test
    void buildWorkflowDoneReachedTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowDoneReachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_DONE_REACHED, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowDoneReachedDetailsInt.class, actual.getDetails())
        );

        WorkflowDoneReachedDetailsInt details = (WorkflowDoneReachedDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId())
        );
    }

    @Test
    void getWorkflowDoneReachedTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowDoneReachedTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertTrue(result.contains(TEST_IUN))
        );
    }

    @Test
    void buildTimeline() {
        // Arrange
        NotificationInt notification = createNotification();
        TimelineElementDetailsInt details = WorkflowEndedUndeliverableDetailsInt.builder()
                .recIndex(TEST_REC_INDEX)
                .build();

        // Act
        TimelineElementInternal actual = timelineUtils.buildTimeline(
                notification, WORKFLOW_ENDED_UNDELIVERABLE, TEST_EVENT_ID, details);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(WORKFLOW_ENDED_UNDELIVERABLE, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> Assertions.assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertEquals(details, actual.getDetails()),
                () -> Assertions.assertNotNull(actual.getNotificationSentAt())
        );
    }

    @Test
    void checkTimelineCategories_shouldReturnTrue_whenCategoryExists() {
        // Arrange
        TimelineElementInternal element = createTimelineElement(DELIVERED, TEST_REC_INDEX);
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of(element));

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertTrue(result);
        verify(timelineService).getTimeline(TEST_IUN, false);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenCategoryDoesNotExist() {
        // Arrange
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of());

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertFalse(result);
        verify(timelineService).getTimeline(TEST_IUN, false);
    }

    @Test
    void checkTimelineCategories_shouldReturnTrue_whenAnyOfMultipleCategoriesExists() {
        // Arrange
        TimelineElementInternal element = createTimelineElement(INFORMAL_NOTIFICATION_VIEWED, TEST_REC_INDEX);
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of(element));

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX,
                DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenNoneOfMultipleCategoriesExists() {
        // Arrange
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of());

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX,
                DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenCategoryExistsButForDifferentRecipient() {
        // Arrange
        int differentRecIndex = 1;
        TimelineElementInternal element = createTimelineElement(DELIVERED, differentRecIndex);
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of(element));

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void checkTimelineCategories_shouldHandleMultipleTimelineElements() {
        // Arrange
        TimelineElementInternal element1 = createTimelineElement(DELIVERED, 0);
        TimelineElementInternal element2 = createTimelineElement(PAYMENT, 1);
        TimelineElementInternal element3 = createTimelineElement(DELIVERED, TEST_REC_INDEX);

        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of(element1, element2, element3));

        // Act
        boolean result = timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void buildTimeline_shouldSetNotificationSentAt() {
        // Arrange
        Instant sentAt = Instant.now().minusSeconds(3600);
        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .sender(NotificationSenderInt.builder().paId(TEST_PA_ID).build())
                .sentAt(sentAt)
                .build();

        TimelineElementDetailsInt details = WorkflowEndedUndeliverableDetailsInt.builder()
                .recIndex(TEST_REC_INDEX)
                .build();

        // Act
        TimelineElementInternal result = timelineUtils.buildTimeline(
                notification, WORKFLOW_ENDED_UNDELIVERABLE, TEST_EVENT_ID, details);

        // Assert
        Assertions.assertEquals(sentAt, result.getNotificationSentAt());
    }

    @Test
    void buildWorkflowEndedReachedTimelineElement_shouldSetNotificationDate() {
        // Arrange
        NotificationInt notification = createNotification();
        Instant before = Instant.now();

        // Act
        TimelineElementInternal result = timelineUtils.buildWorkflowEndedReachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID);

        Instant after = Instant.now();

        // Assert
        WorkflowEndedReachedDetailsInt details = (WorkflowEndedReachedDetailsInt) result.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(details.getNotificationDate()),
                () -> Assertions.assertFalse(details.getNotificationDate().isBefore(before)),
                () -> Assertions.assertFalse(details.getNotificationDate().isAfter(after))
        );
    }

    @Test
    void buildWorkflowEndedUndeliverableTimelineElement_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 5;
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal result = timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(
                recIndex2, notification, TEST_EVENT_ID);

        // Assert
        WorkflowEndedUndeliverableDetailsInt details = (WorkflowEndedUndeliverableDetailsInt) result.getDetails();
        Assertions.assertEquals(recIndex2, details.getRecIndex());
    }

    @Test
    void buildWorkflowDoneReachedTimelineElement_shouldHandleDifferentSourceTimelineIds() {
        // Arrange
        String customSourceId = "CUSTOM-SOURCE-ID";
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal result = timelineUtils.buildWorkflowDoneReachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, customSourceId);

        // Assert
        WorkflowDoneReachedDetailsInt details = (WorkflowDoneReachedDetailsInt) result.getDetails();
        Assertions.assertEquals(customSourceId, details.getSourceElementId());
    }

    @Test
    void buildWorkflowDoneUnreachedTimelineElement_shouldHandleDifferentSourceTimelineIds() {
        // Arrange
        String customSourceId = "CUSTOM-SOURCE-ID";
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal result = timelineUtils.buildWorkflowDoneUnreachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, customSourceId);

        // Assert
        WorkflowDoneUnreachedDetailsInt details = (WorkflowDoneUnreachedDetailsInt) result.getDetails();
        Assertions.assertEquals(customSourceId, details.getSourceElementId());
    }

    private NotificationInt createNotification() {
        return NotificationInt.builder()
                .iun(TEST_IUN)
                .sender(NotificationSenderInt.builder()
                        .paId(TEST_PA_ID)
                        .build())
                .sentAt(Instant.now())
                .build();
    }

    private TimelineElementInternal createTimelineElement(TimelineElementCategoryInt category, int recIndex) {
        RecipientRelatedTimelineElementDetails details = new RecipientRelatedTimelineElementDetails() {
            @Override
            public String toLog() {
                return "";
            }

            @Override
            public void setCategoryType(String category) {

            }

            @Override
            public int getRecIndex() {
                return recIndex;
            }
        };

        return TimelineElementInternal.builder()
                .category(category)
                .details(details)
                .build();
    }
}
