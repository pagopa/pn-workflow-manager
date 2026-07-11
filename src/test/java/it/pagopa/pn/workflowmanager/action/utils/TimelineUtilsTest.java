package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.timelineservice.model.NotificationHistoryResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.timelineservice.model.NotificationStatus;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.*;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
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
                () -> Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, details.getSourceElementId())
        );
    }

    @Test
    void buildSendDigitalMessageSkipTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildSendDigitalMessageSkipTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, DigitalChannelsInt.EMAIL,DigitalAddressSourceInt.SPECIAL);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(SEND_DIGITAL_MESSAGE_SKIP, actual.getCategory()),
                () -> Assertions.assertEquals(TEST_EVENT_ID, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(SendDigitalMessageSkipDetailsInt.class, actual.getDetails())
        );

        SendDigitalMessageSkipDetailsInt details = (SendDigitalMessageSkipDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex())
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
                TEST_REC_INDEX, notification, TEST_EVENT_ID, TEST_SOURCE_TIMELINE_ID, DesiredFeedbackType.SENT);

        // Assert
        Assertions.assertEquals("TEST-IUN-001", actual.getIun());
        Assertions.assertEquals(TEST_PA_ID, actual.getPaId());
        Assertions.assertEquals(TimelineElementCategoryInt.WORKFLOW_DONE_REACHED, actual.getCategory());
        assertNotNull(actual.getDetails());
        WorkflowDoneReachedDetailsInt detailsInt = (WorkflowDoneReachedDetailsInt) actual.getDetails();
        Assertions.assertEquals(TEST_REC_INDEX, detailsInt.getRecIndex());
        Assertions.assertEquals(TEST_SOURCE_TIMELINE_ID, detailsInt.getSourceElementId());
        Assertions.assertEquals(DesiredFeedbackType.SENT.name(), detailsInt.getCompletionFeedback());
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
    void buildWorkflowDoneUnreachedTimelineElement() {
        // Arrange
        String customSourceId = "CUSTOM-SOURCE-ID";
        NotificationInt notification = createNotification();

        // Act
        TimelineElementInternal actual = timelineUtils.buildWorkflowDoneUnreachedTimelineElement(
                TEST_REC_INDEX, notification, TEST_EVENT_ID, customSourceId, DesiredFeedbackType.SENT);

        // Assert
        Assertions.assertEquals("TEST-IUN-001", actual.getIun());
        Assertions.assertEquals(TEST_PA_ID, actual.getPaId());
        Assertions.assertEquals(WORKFLOW_DONE_UNREACHED, actual.getCategory());
        assertNotNull(actual.getDetails());
        WorkflowDoneUnreachedDetailsInt detailsInt = (WorkflowDoneUnreachedDetailsInt) actual.getDetails();
        Assertions.assertEquals(TEST_REC_INDEX, detailsInt.getRecIndex());
        Assertions.assertEquals(customSourceId, detailsInt.getSourceElementId());
        Assertions.assertEquals(DesiredFeedbackType.SENT.name(), detailsInt.getCompletionFeedback());
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
        Instant notificationDate = Instant.now();
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
                sourceId,
                notificationDate
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
        Assertions.assertEquals(notificationDate, detailsInt.getNotificationDate());
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
    void checkAndRetrieveSourceSendRequestDetailsSuccess() {
        // Arrange
        String iun = "TEST-IUN-001";
        String requestId = "request-001";
        int expectedRecIndex = 2;

        TimelineElementInternal mockElement = mock(TimelineElementInternal.class);
        SendRelatedTimelineElement mockDetails = mock(SendRelatedTimelineElement.class);

        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(mockElement));
        when(mockElement.getDetails()).thenReturn(mockDetails);
        when(mockDetails.getRecIndex()).thenReturn(expectedRecIndex);

        // Act
        SendRelatedTimelineElement element = timelineUtils.checkAndRetrieveSourceSendRequestDetails(iun, requestId);

        // Assert
        assertEquals(expectedRecIndex, element.getRecIndex());
        verify(timelineService).getTimelineElement(iun, requestId);
    }

    @Test
    void checkAndRetrieveSourceSendRequestDetailsElementNotFound() {
        // Arrange
        String iun = "TEST-IUN-001";
        String requestId = "request-not-found";

        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PnInternalException.class, () -> timelineUtils.checkAndRetrieveSourceSendRequestDetails(iun, requestId));
    }

    @Test
    void checkAndRetrieveSourceSendRequestDetailsInvalidDetailsType() {
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
        assertThrows(PnInternalException.class, () -> timelineUtils.checkAndRetrieveSourceSendRequestDetails(iun, requestId));
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

    @Test
    void buildPrepareAnalogDeliveryTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();
        String eventId = "PREPARE_ANALOG.IUN_TEST-IUN-001.RECINDEX_0.ATTEMPT_0";
        ServiceLevelInt serviceLevel = ServiceLevelInt.AR_REGISTERED_LETTER;
        Integer sentAttemptMade = 0;
        String relatedRequestId = "related-request-001";
        PhysicalAddressInt physicalAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .addressDetails("Scala A")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .foreignState("ITALIA")
                .build();

        // Act
        TimelineElementInternal actual = timelineUtils.buildPrepareAnalogDeliveryTimelineElement(
                TEST_REC_INDEX,
                notification,
                eventId,
                serviceLevel,
                sentAttemptMade,
                relatedRequestId,
                physicalAddress
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_IUN, actual.getIun()),
                () -> Assertions.assertEquals(PREPARE_ANALOG_DELIVERY, actual.getCategory()),
                () -> Assertions.assertEquals(eventId, actual.getElementId()),
                () -> Assertions.assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
                () -> Assertions.assertInstanceOf(PrepareAnalogDeliveryDetailsInt.class, actual.getDetails())
        );

        PrepareAnalogDeliveryDetailsInt details = (PrepareAnalogDeliveryDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> Assertions.assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> Assertions.assertEquals(physicalAddress, details.getPhysicalAddress()),
                () -> Assertions.assertEquals(AnalogDeliveryTypeInt.RS, details.getDeliveryType()),
                () -> Assertions.assertEquals(serviceLevel, details.getServiceLevel()),
                () -> Assertions.assertEquals(sentAttemptMade, details.getSentAttemptMade()),
                () -> Assertions.assertEquals(relatedRequestId, details.getRelatedRequestId()),
                () -> Assertions.assertEquals("ITALIA", details.getForeignState())
        );
    }

    @Test
    void buildSendAnalogNotificationTimelineElement_AndBuildSendAnalogTimelineEventId() {
        // Arrange
        NotificationInt notification = createNotification();
        PhysicalAddressInt physicalAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .foreignState("ITALIA")
                .build();

        String productType = "AR";
        Integer sentAttemptMade = 0;
        String relatedRequestId = "related-request-001";
        String prepareRequestId = "PREPARE_ANALOG_DELIVERY.IUN_TEST-IUN-001.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS";

        SendResponse sendResponse = mock(SendResponse.class);
        when(sendResponse.getAmount()).thenReturn(5);
        when(sendResponse.getNumberOfPages()).thenReturn(3);
        when(sendResponse.getEnvelopeWeight()).thenReturn(100);

        AnalogDtoInt analogDtoInfo = AnalogDtoInt.builder()
                .sentAttemptMade(sentAttemptMade)
                .sendResponse(sendResponse)
                .relatedRequestId(relatedRequestId)
                .productType(productType)
                .prepareRequestId(prepareRequestId)
                .build();

        List<String> replacedF24AttachmentUrls = List.of("f24-url-1", "f24-url-2");
        CategorizedAttachmentsResultInt categorizedAttachmentsResult = mock(CategorizedAttachmentsResultInt.class);
        ServiceLevelInt serviceLevelInt = ServiceLevelInt.AR_REGISTERED_LETTER;

        // Act - Test buildSendAnalogTimelineEventId
        String eventId = TimelineUtils.buildSendAnalogTimelineEventId(TEST_REC_INDEX, notification, analogDtoInfo);

        // Assert - verify eventId
        assertNotNull(eventId);
        assertTrue(eventId.contains(TEST_IUN));
        assertTrue(eventId.contains("SEND_ANALOG_MESSAGE"));

        // Act - Test buildSendAnalogNotificationTimelineElement
        TimelineElementInternal actual = timelineUtils.buildSendAnalogNotificationTimelineElement(
                physicalAddress, TEST_REC_INDEX, notification, analogDtoInfo,
                replacedF24AttachmentUrls, categorizedAttachmentsResult, serviceLevelInt, prepareRequestId);

        // Assert - verify timeline element
        Assertions.assertAll(
                () -> assertEquals(TEST_IUN, actual.getIun()),
                () -> assertEquals(SEND_ANALOG_MESSAGE, actual.getCategory()),
                () -> assertNotNull(actual.getElementId()),
                () -> assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertNotNull(actual.getDetails()),
                () -> assertInstanceOf(SendAnalogMessageDetailsInt.class, actual.getDetails())
        );

        SendAnalogMessageDetailsInt details = (SendAnalogMessageDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> assertEquals(physicalAddress, details.getPhysicalAddress()),
                () -> assertEquals(serviceLevelInt, details.getServiceLevel()),
                () -> assertEquals(sentAttemptMade, details.getSentAttemptMade()),
                () -> assertEquals(relatedRequestId, details.getRelatedRequestId()),
                () -> assertEquals(5, details.getAnalogCost()),
                () -> assertEquals(productType, details.getProductType()),
                () -> assertEquals(3, details.getNumberOfPages()),
                () -> assertEquals(100, details.getEnvelopeWeight()),
                () -> assertEquals(replacedF24AttachmentUrls, details.getF24Attachments()),
                () -> assertEquals(categorizedAttachmentsResult, details.getCategorizedAttachmentsResult()),
                () -> assertNotNull(details.getPrepareRequestId())
        );
    }

    @Test
    void buildSendAnalogProgressNotificationTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();
        ServiceLevelInt serviceLevel = ServiceLevelInt.AR_REGISTERED_LETTER;
        Instant notificationDate = Instant.now();
        AnalogDeliveryDetailsInt deliveryDetail = AnalogDeliveryDetailsInt.builder()
                .code("P000")
                .eventTimestamp(notificationDate)
                .build();
        AnalogDeliveryTypeInt deliveryType = AnalogDeliveryTypeInt.RS;
        List<AttachmentDetailsInt> attachments = List.of(AttachmentDetailsInt.builder().id("att-1").build());
        String sendRequestId = "req-analog-001";
        String registeredLetterCode = "rlc-001";
        Integer sentAttemptMade = 0;
        Integer progressIndex = 1;

        when(timelineService.retrieveAndIncrementCounterForTimelineEvent(sendRequestId))
                .thenReturn(progressIndex.longValue());

        String expectedEventId = TimelineUtils.buildSendAnalogProgressTimelineEventId(
                TEST_REC_INDEX, notification, progressIndex, deliveryType, sentAttemptMade);

        // Act
        TimelineElementInternal actual = timelineUtils.buildSendAnalogProgressNotificationTimelineElement(
                TEST_REC_INDEX, notification, serviceLevel, notificationDate,
                deliveryDetail, deliveryType, attachments, sendRequestId, registeredLetterCode, sentAttemptMade);

        // Assert
        verify(timelineService).retrieveAndIncrementCounterForTimelineEvent(sendRequestId);
        Assertions.assertAll(
                () -> assertEquals(TEST_IUN, actual.getIun()),
                () -> assertEquals(SEND_ANALOG_MESSAGE_PROGRESS, actual.getCategory()),
                () -> assertEquals(expectedEventId, actual.getElementId()),
                () -> assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertInstanceOf(SendAnalogMessageProgressDetailsInt.class, actual.getDetails())
        );

        SendAnalogMessageProgressDetailsInt details = (SendAnalogMessageProgressDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> assertEquals(serviceLevel, details.getServiceLevel()),
                () -> assertEquals(attachments, details.getAttachments()),
                () -> assertEquals(sendRequestId, details.getSendRequestId()),
                () -> assertEquals(deliveryDetail, details.getDeliveryDetail()),
                () -> assertEquals(registeredLetterCode, details.getRegisteredLetterCode()),
                () -> assertEquals(notificationDate, details.getNotificationDate()),
                () -> assertEquals(deliveryType, details.getDeliveryType()),
                () -> assertEquals(sentAttemptMade, details.getSentAttemptMade())
        );
    }

    @Test
    void buildSendAnalogProgressTimelineEventId() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        String result = TimelineUtils.buildSendAnalogProgressTimelineEventId(
                TEST_REC_INDEX, notification, 1, AnalogDeliveryTypeInt.RS, 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(TEST_IUN));
    }

    @Test
    void buildSendAnalogFeedbackNotificationTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();
        ServiceLevelInt serviceLevel = ServiceLevelInt.AR_REGISTERED_LETTER;
        Instant notificationDate = Instant.now();
        AnalogDeliveryDetailsInt deliveryDetail = AnalogDeliveryDetailsInt.builder()
                .code("CON080")
                .eventTimestamp(notificationDate)
                .build();
        AnalogDeliveryTypeInt deliveryType = AnalogDeliveryTypeInt.RS;
        List<AttachmentDetailsInt> attachments = List.of(AttachmentDetailsInt.builder().id("att-1").build());
        String sendRequestId = "req-analog-001";
        String registeredLetterCode = "rlc-001";
        PhysicalAddressInt physicalAddress = PhysicalAddressInt.builder().address("Via Roma 1").build();
        PhysicalAddressInt newAddress = PhysicalAddressInt.builder().address("Via Milano 5").build();
        ResponseStatusInt responseStatus = ResponseStatusInt.OK;
        Integer sentAttemptMade = 0;

        String expectedEventId = TimelineUtils.buildSendAnalogFeedbackTimelineEventId(
                TEST_REC_INDEX, notification, deliveryType, sentAttemptMade);

        // Act
        TimelineElementInternal actual = timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                TEST_REC_INDEX, notification, serviceLevel, notificationDate,
                deliveryDetail, deliveryType, attachments, sendRequestId, registeredLetterCode,
                physicalAddress, responseStatus, newAddress, sentAttemptMade);

        // Assert
        Assertions.assertAll(
                () -> assertEquals(TEST_IUN, actual.getIun()),
                () -> assertEquals(SEND_ANALOG_MESSAGE_FEEDBACK, actual.getCategory()),
                () -> assertEquals(expectedEventId, actual.getElementId()),
                () -> assertEquals(TEST_PA_ID, actual.getPaId()),
                () -> assertNotNull(actual.getTimestamp()),
                () -> assertInstanceOf(SendAnalogMessageFeedbackDetailsInt.class, actual.getDetails())
        );

        SendAnalogMessageFeedbackDetailsInt details = (SendAnalogMessageFeedbackDetailsInt) actual.getDetails();
        Assertions.assertAll(
                () -> assertEquals(TEST_REC_INDEX, details.getRecIndex()),
                () -> assertEquals(serviceLevel, details.getServiceLevel()),
                () -> assertEquals(physicalAddress, details.getPhysicalAddress()),
                () -> assertEquals(newAddress, details.getNewAddress()),
                () -> assertEquals(attachments, details.getAttachments()),
                () -> assertEquals(sendRequestId, details.getSendRequestId()),
                () -> assertEquals(sentAttemptMade, details.getSentAttemptMade()),
                () -> assertEquals(responseStatus, details.getResponseStatus()),
                () -> assertNull(details.getRequestTimelineId()),
                () -> assertEquals(deliveryDetail, details.getDeliveryDetail()),
                () -> assertEquals(registeredLetterCode, details.getRegisteredLetterCode()),
                () -> assertEquals(notificationDate, details.getNotificationDate()),
                () -> assertEquals(deliveryType, details.getDeliveryType())
        );
    }

    @Test
    void buildSendAnalogFeedbackTimelineEventId() {
        // Arrange
        NotificationInt notification = createNotification();

        // Act
        String result = TimelineUtils.buildSendAnalogFeedbackTimelineEventId(
                TEST_REC_INDEX, notification, AnalogDeliveryTypeInt.RS, 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(TEST_IUN));
    }

    @Test
    void buildPaymentTimelineElement() {
        // Arrange
        NotificationInt notification = createNotification();
        int recIndex = 0;
        String noticeCode = "NOTICE_123";
        String creditorTaxId = "CREDITOR_TAX_ID";
        int amount = 100;
        String paymentSourceChannel = "PPA";
        Instant eventTimestamp = Instant.now();
        NotificationPaidInt notificationPaid = NotificationPaidInt.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .noticeCode(noticeCode)
                .creditorTaxId(creditorTaxId)
                .amount(amount)
                .paymentSourceChannel(paymentSourceChannel)
                .eventTimestamp(eventTimestamp)
                .build();

        String expectedElementId = TimelineEventId.NOTIFICATION_PAID.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .noticeCode(noticeCode)
                        .creditorTaxId(creditorTaxId)
                        .build()
        );

        // Act
        TimelineElementInternal actual = timelineUtils.buildPaymentTimelineElement(notification, notificationPaid, notification.getRecipients().getFirst());

        // Assert
        assertNotNull(actual);
        assertEquals(notification.getIun(), actual.getIun());
        assertEquals(expectedElementId, actual.getElementId());
        assertEquals(PAYMENT, actual.getCategory());
        assertNotNull(actual.getDetails());

        NotificationPaidDetailsInt details = (NotificationPaidDetailsInt) actual.getDetails();
        assertEquals(recIndex, details.getRecIndex());
        assertEquals(RecipientTypeInt.PF.name(), details.getRecipientType());
        assertEquals(amount, details.getAmount());
        assertEquals(noticeCode, details.getNoticeCode());
        assertEquals(paymentSourceChannel, details.getPaymentSourceChannel());
        assertFalse(details.isUncertainPaymentDate());
        assertEquals(eventTimestamp, details.getEventTimestamp());
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
