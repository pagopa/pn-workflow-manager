package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NotificationHistoryResponse;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NotificationStatus;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.*;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
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
                () -> assertNotNull(result),
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
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
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
                () -> assertNotNull(result),
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
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(WorkflowEndedReachedDetailsInt.class, actual.getDetails())
        );

        WorkflowEndedReachedDetailsInt details = (WorkflowEndedReachedDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId()),
                () -> assertNotNull(details.getNotificationDate())
        );
    }

    @Test
    void getWorkflowEndedReachedTimelineElementId() {
        // Act
        String result = TimelineUtils.getWorkflowEndedReachedTimelineElementId(TEST_REC_INDEX, TEST_IUN);

        // Assert
        Assertions.assertAll(
                () -> assertNotNull(result),
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
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
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
                () -> assertNotNull(result),
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
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
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
                () -> assertNotNull(result),
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
                () -> assertNotNull(actual.getTimestamp()),
                () -> Assertions.assertEquals(details, actual.getDetails()),
                () -> assertNotNull(actual.getNotificationSentAt())
        );
    }

    @Test
    void checkTimelineCategories_shouldReturnTrue_whenCategoryExists() {
        // Arrange
        TimelineElementInternal element = createTimelineElement(DELIVERED, TEST_REC_INDEX);
        List<TimelineElementInternal> timelineElements = List.of(element);

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenCategoryDoesNotExist() {
        // Arrange
        List<TimelineElementInternal> timelineElements = List.of();

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnTrue_whenAnyOfMultipleCategoriesExists() {
        // Arrange
        TimelineElementInternal element = createTimelineElement(INFORMAL_NOTIFICATION_VIEWED, TEST_REC_INDEX);
        List<TimelineElementInternal> timelineElements = List.of(element);

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX,
                DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenNoneOfMultipleCategoriesExists() {
        // Arrange
        List<TimelineElementInternal> timelineElements = List.of();

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX,
                DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void checkTimelineCategories_shouldReturnFalse_whenCategoryExistsButForDifferentRecipient() {
        // Arrange
        int differentRecIndex = 1;
        TimelineElementInternal element = createTimelineElement(DELIVERED, differentRecIndex);
        List<TimelineElementInternal> timelineElements = List.of(element);

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    void checkTimelineCategories_shouldHandleMultipleTimelineElements() {
        // Arrange
        TimelineElementInternal element1 = createTimelineElement(DELIVERED, 0);
        TimelineElementInternal element2 = createTimelineElement(PAYMENT, 1);
        TimelineElementInternal element3 = createTimelineElement(DELIVERED, TEST_REC_INDEX);

        List<TimelineElementInternal> timelineElements = List.of(element1, element2, element3);

        // Act
        boolean result = timelineUtils.checkTimelineCategories(timelineElements, TEST_REC_INDEX, DELIVERED);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void getTimelineElementInternals_shouldReturnStreamFromService() {
        // Arrange
        TimelineElementInternal element1 = createTimelineElement(DELIVERED, TEST_REC_INDEX);
        TimelineElementInternal element2 = createTimelineElement(PAYMENT, TEST_REC_INDEX);
        Set<TimelineElementInternal> timelineSet = Set.of(element1, element2);

        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(timelineSet);

        // Act
        List<TimelineElementInternal> result = timelineUtils.getTimelineElementInternals(TEST_IUN).toList();

        // Assert
        Assertions.assertEquals(2, result.size());
        verify(timelineService).getTimeline(TEST_IUN, false);
    }

    @Test
    void getTimelineElementInternals_shouldReturnEmptyStream_whenTimelineIsEmpty() {
        // Arrange
        when(timelineService.getTimeline(TEST_IUN, false)).thenReturn(Set.of());

        // Act
        List<TimelineElementInternal> result = timelineUtils.getTimelineElementInternals(TEST_IUN).toList();

        // Assert
        Assertions.assertTrue(result.isEmpty());
        verify(timelineService).getTimeline(TEST_IUN, false);
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
                () -> assertNotNull(details.getNotificationDate()),
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

    @Test
    void retrieveCoverpageFileKeyReturnsFileKeyFromTimelineElement() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );
        String expectedFileKey = "coverpage-file-key";

        TimelineElementInternal timelineElement = TimelineElementInternal.builder()
                .details(CoverpageCreationRequestDetailsInt.builder()
                        .recIndex(recIndex)
                        .fileKey(expectedFileKey)
                        .build())
                .build();

        when(timelineService.getTimelineElement(iun, expectedTimelineId))
                .thenReturn(Optional.of(timelineElement));

        String result = timelineUtils.retrieveCoverpageFileKey(iun, recIndex);

        assertEquals(expectedFileKey, result);
        verify(timelineService).getTimelineElement(iun, expectedTimelineId);
    }

    @Test
    void retrieveCoverpageFileKeyThrowsWhenTimelineElementIsMissing() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        when(timelineService.getTimelineElement(iun, expectedTimelineId))
                .thenReturn(Optional.empty());

        assertThrows(
                PnInternalException.class,
                () -> timelineUtils.retrieveCoverpageFileKey(iun, recIndex)
        );
    }

    @Test
    void retrieveCoverpageFileKeyThrowsWhenFileKeyIsBlank() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        TimelineElementInternal timelineElement = TimelineElementInternal.builder()
                .details(CoverpageCreationRequestDetailsInt.builder()
                        .recIndex(recIndex)
                        .fileKey(" ")
                        .build())
                .build();

        when(timelineService.getTimelineElement(iun, expectedTimelineId))
                .thenReturn(Optional.of(timelineElement));

        assertThrows(
                PnInternalException.class,
                () -> timelineUtils.retrieveCoverpageFileKey(iun, recIndex)
        );
    }

    @Test
    void buildSendDigitalMessageTimelineElement() {
        String elementId = "send_digital_message_001";
        int recIndex = 0;
        InformalDigitalAddressInt informalDigitalAddressInt = InformalDigitalAddressInt.builder()
                .address("address")
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();
        DigitalChannelsInt digitalChannelsInt = DigitalChannelsInt.PEC;
        DigitalAddressSourceInt digitalAddressSourceInt = DigitalAddressSourceInt.SPECIAL;
        TimelineElementInternal actual = timelineUtils.buildSendDigitalMessageTimelineElement(
                createNotification(),
                elementId,
                recIndex,
                informalDigitalAddressInt,
                digitalChannelsInt,
                digitalAddressSourceInt
        );
        Assertions.assertEquals("TEST-IUN-001", actual.getIun());
        Assertions.assertEquals(elementId, actual.getElementId());
        Assertions.assertEquals(TEST_PA_ID, actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, actual.getCategory());
        assertNotNull(actual.getDetails());
        SendDigitalMessageDetailsInt detailsInt = (SendDigitalMessageDetailsInt) actual.getDetails();
        Assertions.assertEquals(recIndex, detailsInt.getRecIndex());
        Assertions.assertEquals(informalDigitalAddressInt, detailsInt.getDigitalAddress());
        Assertions.assertEquals(digitalChannelsInt, detailsInt.getChannel());
        Assertions.assertEquals(digitalAddressSourceInt, detailsInt.getDigitalAddressSource());
    }

    @Test
    void buildDeliveredTimelineElement() {
        String sourceId = "source_001";
        int recIndex = 0;
        NotificationInt notification = createNotification();
        ChannelType channel = ChannelType.IO;
        String expectedElementId = TimelineEventId.DELIVERED.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );
        TimelineElementInternal actual = timelineUtils.buildDeliveredTimelineElement(
                createNotification(),
                recIndex,
                channel,
                sourceId
        );

        Assertions.assertEquals("TEST-IUN-001", actual.getIun());
        Assertions.assertEquals(expectedElementId, actual.getElementId());
        Assertions.assertEquals(TEST_PA_ID, actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.DELIVERED, actual.getCategory());
        assertNotNull(actual.getDetails());
        DeliveredDetailsInt detailsInt = (DeliveredDetailsInt) actual.getDetails();
        Assertions.assertEquals(recIndex, detailsInt.getRecIndex());
        Assertions.assertEquals(ChannelType.IO.name(), detailsInt.getChannel());
        Assertions.assertEquals(sourceId, detailsInt.getSourceElementId());
    }

    @Test
    void handleTransitionToReachedStatusIfNecessaryPersistWorkflowEndedReachedElement() {
        NotificationInt notification = createNotification();
        int recIndex = 0;
        String sourceId = "source_001";

        NotificationHistoryResponse historyResponse = new NotificationHistoryResponse();
        historyResponse.setNotificationStatus(NotificationStatus.COMPLETED_UNREACHED);
        when(timelineService.getTimelineAndStatusHistory(notification.getIun(), notification.getRecipients().size(), notification.getSentAt()))
                        .thenReturn(historyResponse);

        timelineUtils.handleTransitionToReachedStatusIfNecessary(notification, recIndex, sourceId);

        ArgumentCaptor<TimelineElementInternal> elementCaptor = ArgumentCaptor.forClass(TimelineElementInternal.class);
        verify(timelineService).addTimelineElement(elementCaptor.capture(), eq(notification));
        TimelineElementInternal capturedElement = elementCaptor.getValue();
        assertEquals(WORKFLOW_ENDED_REACHED, capturedElement.getCategory());
        WorkflowEndedReachedDetailsInt details = (WorkflowEndedReachedDetailsInt) capturedElement.getDetails();
        assertEquals(recIndex, details.getRecIndex());
        assertEquals(sourceId, details.getSourceElementId());
    }

    @Test
    void handleTransitionToReachedStatusIfNecessaryDoesntPersistWorkflowEndedReachedElement() {
        NotificationInt notification = createNotification();
        int recIndex = 0;
        String sourceId = "source_001";

        NotificationHistoryResponse historyResponse = new NotificationHistoryResponse();
        historyResponse.setNotificationStatus(NotificationStatus.PROCESSING);
        when(timelineService.getTimelineAndStatusHistory(notification.getIun(), notification.getRecipients().size(), notification.getSentAt()))
                .thenReturn(historyResponse);

        timelineUtils.handleTransitionToReachedStatusIfNecessary(notification, recIndex, sourceId);

        verify(timelineService).getTimelineAndStatusHistory(notification.getIun(), notification.getRecipients().size(), notification.getSentAt());
        verify(timelineService, never()).addTimelineElement(org.mockito.Mockito.any(), org.mockito.Mockito.eq(notification));
    }

    @Test
    void buildSendDigitalMessageProgressSuccess() {
        NotificationInt notification = createNotification();
        int recIndex = 0;
        DigitalChannelsInt channel = DigitalChannelsInt.PEC;
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code("TEST-CODE")
                .build();
        String requestId = "request-001";
        InformalDigitalAddressInt digitalAddress = InformalDigitalAddressInt.builder()
                .address("address")
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();
        DigitalAddressSourceInt digitalAddressSource = DigitalAddressSourceInt.SPECIAL;
        Instant eventTimestamp = Instant.now();

        when(timelineService.retrieveAndIncrementCounterForTimelineEvent(requestId))
                .thenReturn(1L);

        String expectedElementId = TimelineEventId.SEND_DIGITAL_MESSAGE_PROGRESS.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .progressIndex(1)
                        .build()
        );

        // Act
        TimelineElementInternal actual = timelineUtils.buildSendDigitalMessageProgress(
                notification,
                recIndex,
                channel,
                requestId,
                deliveryDetail,
                digitalAddress,
                digitalAddressSource,
                eventTimestamp
        );

        // Assert - Verifica delle interazioni fondamentali
        verify(timelineService).retrieveAndIncrementCounterForTimelineEvent(requestId);

        // Assert - Validazione dell'elemento ritornato (Mock o reale che sia)
        assertNotNull(actual);
        assertEquals(notification.getIun(), actual.getIun());
        assertEquals(expectedElementId, actual.getElementId());
        assertEquals(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS, actual.getCategory());
        assertNotNull(actual.getDetails());

        SendDigitalMessageProgressDetailsInt details = (SendDigitalMessageProgressDetailsInt) actual.getDetails();
        assertEquals(recIndex, details.getRecIndex());
        assertEquals(requestId, details.getRequestId());
        assertEquals(channel, details.getChannel());
        assertEquals(deliveryDetail, details.getDeliveryDetail());
        assertEquals(digitalAddress, details.getDigitalAddress());
        assertEquals(digitalAddressSource, details.getDigitalAddressSource());
        assertEquals(eventTimestamp, details.getEventTimestamp());
    }

    @Test
    void buildSendDigitalMessageFeedbackSuccess() {
        // Arrange
        NotificationInt notification = createNotification();
        int recIndex = 0;
        DigitalChannelsInt channel = DigitalChannelsInt.PEC;
        DigitalDeliveryDetailsInt deliveryDetail = DigitalDeliveryDetailsInt.builder()
                .code("TEST-CODE")
                .build();
        String requestId = "request-001";
        InformalDigitalAddressInt digitalAddress = InformalDigitalAddressInt.builder()
                .address("address")
                .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                .build();
        DigitalAddressSourceInt digitalAddressSource = DigitalAddressSourceInt.SPECIAL;
        ResponseStatusInt responseStatus = ResponseStatusInt.OK;
        List<SendingReceipt> sendingReceipts = List.of(new SendingReceipt());
        Instant eventTimestamp = Instant.now();

        String expectedElementId = TimelineEventId.SEND_DIGITAL_MESSAGE_FEEDBACK.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );

        // Act
        TimelineElementInternal actual = timelineUtils.buildSendDigitalMessageFeedback(
                notification,
                recIndex,
                channel,
                requestId,
                deliveryDetail,
                digitalAddress,
                digitalAddressSource,
                responseStatus,
                sendingReceipts,
                eventTimestamp
        );

        // Assert
        assertNotNull(actual);
        assertEquals(notification.getIun(), actual.getIun());
        assertEquals(expectedElementId, actual.getElementId());
        assertEquals(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK, actual.getCategory());
        assertNotNull(actual.getDetails());

        SendDigitalMessageFeedbackDetailsInt details = (SendDigitalMessageFeedbackDetailsInt) actual.getDetails();
        assertEquals(recIndex, details.getRecIndex());
        assertEquals(requestId, details.getRequestId());
        assertEquals(channel, details.getChannel());
        assertEquals(deliveryDetail, details.getDeliveryDetail());
        assertEquals(digitalAddress, details.getDigitalAddress());
        assertEquals(digitalAddressSource, details.getDigitalAddressSource());
        assertEquals(responseStatus, details.getResponseStatus());
        assertEquals(sendingReceipts, details.getSendingReceipts());
        assertEquals(eventTimestamp, details.getNotificationDate());
    }

    @Test
    void getIunFromTimelineIdSuccess() {
        // Arrange
        // Formato atteso dal codice: CATEGORY_VALUE.IUN_VALORE-IUN-123.RECINDEX_0
        String timelineId = "SEND_DIGITAL_MESSAGE_PROGRESS.IUN_TEST-IUN-001.RECINDEX_0";

        // Act
        String actualIun = timelineUtils.getIunFromTimelineId(timelineId);

        // Assert
        assertEquals("TEST-IUN-001", actualIun);
    }

    @Test
    void checkIfSendRequestIsPresentAndRetrieveRecIndexSuccess() {
        // Arrange
        String iun = "TEST-IUN-001";
        String requestId = "request-001";
        int expectedRecIndex = 2;

        TimelineElementInternal mockElement = mock(TimelineElementInternal.class);
        RecipientRelatedTimelineElementDetails mockDetails = mock(RecipientRelatedTimelineElementDetails.class);

        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(mockElement));
        when(mockElement.getDetails()).thenReturn(mockDetails);
        when(mockDetails.getRecIndex()).thenReturn(expectedRecIndex);

        // Act
        int actualRecIndex = timelineUtils.checkIfSendRequestIsPresentAndRetrieveRecIndex(iun, requestId);

        // Assert
        assertEquals(expectedRecIndex, actualRecIndex);
        verify(timelineService).getTimelineElement(iun, requestId);
    }

    @Test
    void checkIfSendRequestIsPresentAndRetrieveRecIndexElementNotFound() {
        // Arrange
        String iun = "TEST-IUN-001";
        String requestId = "request-not-found";

        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PnInternalException.class, () -> timelineUtils.checkIfSendRequestIsPresentAndRetrieveRecIndex(iun, requestId));
    }

    @Test
    void checkIfSendRequestIsPresentAndRetrieveRecIndexInvalidDetailsType() {
        // Arrange
        String iun = "TEST-IUN-001";
        String requestId = "request-invalid-details";

        TimelineElementInternal mockElement = mock(TimelineElementInternal.class);
        class InvalidTimelineElementDetails implements TimelineElementDetailsInt {
            @Override
            public String toLog() {
                return "Invalid details";
            }

            @Override
            public void setCategoryType(String category) {

            }
        }

        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(mockElement));
        when(mockElement.getDetails()).thenReturn(new InvalidTimelineElementDetails());

        // Act & Assert
        assertThrows(PnInternalException.class, () -> timelineUtils.checkIfSendRequestIsPresentAndRetrieveRecIndex(iun, requestId));
    }

    private NotificationInt createNotification() {
        return NotificationInt.builder()
                .iun(TEST_IUN)
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .denomination("Test Recipient")
                                .recipientType(RecipientTypeInt.PF)
                                .build()
                ))
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
