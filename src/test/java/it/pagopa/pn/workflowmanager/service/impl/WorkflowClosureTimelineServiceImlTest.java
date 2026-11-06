package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InvoiceableTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.WorkflowDoneReachedDetailsInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.service.WorkflowClosureTimelineService;
import it.pagopa.pn.workflowmanager.utils.WorkflowClosureTimelineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WorkflowClosureTimelineServiceImlTest {

    private static final int REC_INDEX = 0;
    private static final int BASE_COST = 250;

    private TimelineService timelineService;
    private WorkflowClosureTimelineServiceIml service;
    private NotificationInt notification;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);

        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        configs.setPagoPaInformalNotificationBaseCost(BASE_COST);

        TimelineUtils timelineUtils = new TimelineUtils(
                timelineService,
                mock(WorkflowClosureTimelineService.class)
        );
        service = new WorkflowClosureTimelineServiceIml(
                new WorkflowClosureTimelineUtils(configs),
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

    private void saveAllClosureElements(List<TimelineElementInternal> timelineElements) {
        service.saveWorkflowDoneReachedTimelineElement(
                REC_INDEX, "SOURCE_ID", notification, DesiredFeedbackType.RECEIVED, timelineElements);
        service.saveWorkflowDoneUnreachedTimelineElement(
                REC_INDEX, "SOURCE_ID", notification, DesiredFeedbackType.RECEIVED, timelineElements);
        service.saveWorkflowEndedReachedTimelineElement(
                REC_INDEX, "SOURCE_ID", notification, timelineElements);
        service.saveWorkflowEndedUnreachedTimelineElement(
                REC_INDEX, notification, timelineElements);
        service.saveWorkflowEndedUndeliverableTimelineElement(
                REC_INDEX, notification, timelineElements);
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