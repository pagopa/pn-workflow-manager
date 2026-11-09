package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InvoiceableTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.WorkflowDoneReachedDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.WorkflowEndedReachedDetailsInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.timelineservice.model.NotificationStatus;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.utils.NotificationCostUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowClosureTimelineServiceImlTest {

    private static final int REC_INDEX = 0;
    private static final int BASE_COST = 250;
    private static final String TEST_IUN = "TEST-IUN-001";
    private static final String TEST_PA_ID = "PA-001";

    private TimelineService timelineService;
    private WorkflowClosureTimelineServiceIml service;
    private NotificationInt notification;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);

        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        configs.setPagoPaInformalNotificationBaseCost(BASE_COST);

        TimelineUtils timelineUtils = new TimelineUtils(timelineService);
        service = new WorkflowClosureTimelineServiceIml(
                new NotificationCostUtils(configs),
                timelineUtils,
                timelineService
        );

        notification = NotificationInt.builder()
                .iun("IUN_TEST")
                .sender(NotificationSenderInt.builder().paId("PA_TEST").build())
                .build();
    }

    @Test
    void savesAllClosureElementsWithBaseCostWhenTimelineHasNoInvoiceableElement() {
        saveAllClosureElements(List.of());

        List<TimelineElementInternal> savedElements = captureSavedElements();
        assertClosureCategories(savedElements);

        savedElements.forEach(element -> {
            InvoiceableTimelineElement details =
                    assertInstanceOf(InvoiceableTimelineElement.class, element.getDetails());
            assertEquals(BASE_COST, details.getNotificationCost());
        });
    }

    @Test
    void savesAllClosureElementsWithoutAdditionalCostWhenTimelineIsAlreadyInvoiceable() {
        TimelineElementInternal existingInvoiceableElement = TimelineElementInternal.builder()
                .details(WorkflowDoneReachedDetailsInt.builder()
                        .notificationCost(BASE_COST)
                        .build())
                .build();

        saveAllClosureElements(List.of(existingInvoiceableElement));

        List<TimelineElementInternal> savedElements = captureSavedElements();
        assertClosureCategories(savedElements);

        savedElements.forEach(element -> {
            InvoiceableTimelineElement details =
                    assertInstanceOf(InvoiceableTimelineElement.class, element.getDetails());
            assertNull(details.getNotificationCost());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void savesAllClosureElementsApplyingCostOnlyToRecipientsNotAlreadyCharged(int recIndex) {
        TimelineElementInternal existingInvoiceableElement = TimelineElementInternal.builder()
                .iun(notification.getIun())
                .details(WorkflowDoneReachedDetailsInt.builder()
                        .recIndex(0)
                        .notificationCost(BASE_COST)
                        .build())
                .build();

        saveAllClosureElements(List.of(existingInvoiceableElement), recIndex);

        List<TimelineElementInternal> savedElements = captureSavedElements();
        assertClosureCategories(savedElements);
        Integer expectedCost = recIndex == 0 ? null : BASE_COST;

        savedElements.forEach(element -> {
            InvoiceableTimelineElement details =
                    assertInstanceOf(InvoiceableTimelineElement.class, element.getDetails());
            assertEquals(recIndex, details.getRecIndex());
            assertEquals(expectedCost, details.getNotificationCost());
        });
    }

    @ParameterizedTest
    @EnumSource(value = NotificationStatus.class, names = {"COMPLETED_UNREACHED", "UNDELIVERABLE"})
    void handleTransitionToReachedStatusIfNecessaryPersistWorkflowEndedReachedElement(NotificationStatus status) {
        NotificationInt testNotification = createNotification();
        int recIndex = 0;
        String sourceId = "source_001";

        NotificationHistoryResponseInt historyResponse = NotificationHistoryResponseInt.builder()
                .notificationStatus(NotificationStatusInt.valueOf(status.getValue()))
                .notificationStatusHistory(List.of())
                .timeline(List.of())
                .build();
        when(timelineService.getTimelineAndStatusHistory(testNotification.getIun(), testNotification.getRecipients().size(), testNotification.getSentAt()))
                .thenReturn(historyResponse);

        service.handleTransitionToReachedStatusIfNecessary(testNotification, recIndex, sourceId);

        ArgumentCaptor<TimelineElementInternal> captor = ArgumentCaptor.forClass(TimelineElementInternal.class);
        verify(timelineService).addTimelineElement(captor.capture(), eq(testNotification));
        TimelineElementInternal savedElement = captor.getValue();
        assertEquals(TimelineElementCategoryInt.WORKFLOW_ENDED_REACHED, savedElement.getCategory());
        WorkflowEndedReachedDetailsInt details =
                assertInstanceOf(WorkflowEndedReachedDetailsInt.class, savedElement.getDetails());
        assertEquals(recIndex, details.getRecIndex());
        assertEquals(sourceId, details.getSourceElementId());
        assertEquals(BASE_COST, details.getNotificationCost());
    }

    @Test
    void handleTransitionToReachedStatusIfNecessaryDoesntPersistWorkflowEndedReachedElement() {
        NotificationInt testNotification = createNotification();
        int recIndex = 0;
        String sourceId = "source_001";

        NotificationHistoryResponseInt historyResponse = NotificationHistoryResponseInt.builder()
                .notificationStatus(NotificationStatusInt.PROCESSING)
                .notificationStatusHistory(List.of())
                .timeline(List.of())
                .build();
        when(timelineService.getTimelineAndStatusHistory(testNotification.getIun(), testNotification.getRecipients().size(), testNotification.getSentAt()))
                .thenReturn(historyResponse);

        service.handleTransitionToReachedStatusIfNecessary(testNotification, recIndex, sourceId);

        verify(timelineService).getTimelineAndStatusHistory(testNotification.getIun(), testNotification.getRecipients().size(), testNotification.getSentAt());
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    private void saveAllClosureElements(List<TimelineElementInternal> timelineElements) {
        saveAllClosureElements(timelineElements, REC_INDEX);
    }

    private void saveAllClosureElements(List<TimelineElementInternal> timelineElements, int recIndex) {
        service.saveWorkflowDoneReachedTimelineElement(
                recIndex, "SOURCE_ID", notification, DesiredFeedbackType.RECEIVED, timelineElements);
        service.saveWorkflowDoneUnreachedTimelineElement(
                recIndex, "SOURCE_ID", notification, DesiredFeedbackType.RECEIVED, timelineElements);
        service.saveWorkflowEndedReachedTimelineElement(
                recIndex, "SOURCE_ID", notification, timelineElements);
        service.saveWorkflowEndedUnreachedTimelineElement(
                recIndex, notification, timelineElements);
        service.saveWorkflowEndedUndeliverableTimelineElement(
                recIndex, notification, timelineElements);
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

    private List<TimelineElementInternal> captureSavedElements() {
        ArgumentCaptor<TimelineElementInternal> captor =
                ArgumentCaptor.forClass(TimelineElementInternal.class);
        verify(timelineService, times(5)).addTimelineElement(captor.capture(), eq(notification));
        return captor.getAllValues();
    }

    private void assertClosureCategories(List<TimelineElementInternal> elements) {
        assertEquals(List.of(
                TimelineElementCategoryInt.WORKFLOW_DONE_REACHED,
                TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED,
                TimelineElementCategoryInt.WORKFLOW_ENDED_REACHED,
                TimelineElementCategoryInt.WORKFLOW_ENDED_UNREACHED,
                TimelineElementCategoryInt.WORKFLOW_ENDED_UNDELIVERABLE
        ), elements.stream().map(TimelineElementInternal::getCategory).toList());
    }
}