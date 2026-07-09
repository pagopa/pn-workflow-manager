package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.action.utils.TimelineUtils.getInformalNotificationViewedTimelineElementId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationViewedHandlerTest {

    @Mock
    private TimelineService timelineService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CampaignService campaignService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private WorkflowUtils workflowUtils;

    @InjectMocks
    private NotificationViewedHandler handler;

    @Test
    void shouldSkipWhenViewedTimelineAlreadyExists() {
        NotificationViewedInt payload = NotificationViewedInt.builder()
                .iun("IUN_1")
                .recipientIndex(0)
                .sourceChannel("WEB")
                .viewedDate(Instant.now())
                .build();

        String viewedTimelineId = getInformalNotificationViewedTimelineElementId(0, "IUN_1", "WEB");
        when(timelineService.getTimelineElement("IUN_1", viewedTimelineId))
                .thenReturn(Optional.of(TimelineElementInternal.builder().build()));

        handler.handleViewNotification(payload);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(workflowUtils);
        verifyNoInteractions(auditLogService);
        verify(timelineService, never()).addTimelineElement(any(), any());
    }

    @Test
    void shouldPersistViewedAndScheduleWorkflowDoneForWebWhenStopOnViewed() {
        Instant viewedAt = Instant.now();
        NotificationViewedInt payload = NotificationViewedInt.builder()
                .iun("IUN_2")
                .recipientIndex(1)
                .sourceChannel("WEB")
                .sourceChannelDetails("PORTAL")
                .viewedDate(viewedAt)
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_2")
                .campaignId("CMP_1")
                .sender(NotificationSenderInt.builder().paId("PA_1").build())
                .recipients(List.of(NotificationRecipientInt.builder().build(), NotificationRecipientInt.builder().build()))
                .sentAt(Instant.now())
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId("CMP_1")
                .senderId("PA_1")
                .stopOnViewed(true)
                .build();

        String viewedTimelineId = getInformalNotificationViewedTimelineElementId(1, "IUN_2", "WEB");
        TimelineElementInternal viewedElement = TimelineElementInternal.builder()
                .elementId(viewedTimelineId)
                .build();

        when(timelineService.getTimelineElement("IUN_2", viewedTimelineId))
                .thenReturn(Optional.empty());
        when(notificationService.getInformalNotificationByIun("IUN_2")).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId("CMP_1", "PA_1")).thenReturn(campaign);
        when(timelineUtils.buildInformalNotificationViewedTimelineElement(
                notification,
                1,
                viewedTimelineId,
                viewedAt,
                "WEB",
                "PORTAL"
        )).thenReturn(viewedElement);

        handler.handleViewNotification(payload);

        verify(timelineService).addTimelineElement(viewedElement, notification);
        verify(timelineUtils).handleTransitionToReachedStatusIfNecessary(notification, 1, viewedTimelineId);
        verify(auditLogService).buildAuditLogEvent(
                eq("IUN_2"),
                eq(1),
                eq(PnAuditLogEventType.AUD_COM_VIEW_RCP),
                any(),
                eq(1)
        );
        verify(workflowUtils).scheduleWorkflowDone("IUN_2", 1, viewedTimelineId, DesiredFeedbackType.READ);
    }

    @Test
    void shouldPersistViewedWithoutSchedulingWhenNotWeb() {
        NotificationViewedInt payload = NotificationViewedInt.builder()
                .iun("IUN_3")
                .recipientIndex(0)
                .sourceChannel("IO")
                .viewedDate(Instant.now())
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_3")
                .campaignId("CMP_2")
                .sender(NotificationSenderInt.builder().paId("PA_2").build())
                .recipients(List.of(NotificationRecipientInt.builder().build()))
                .sentAt(Instant.now())
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId("CMP_2")
                .senderId("PA_2")
                .stopOnViewed(true)
                .build();

        String viewedTimelineId = getInformalNotificationViewedTimelineElementId(0, "IUN_3", "IO");

        when(timelineService.getTimelineElement("IUN_3", viewedTimelineId))
                .thenReturn(Optional.empty());
        when(notificationService.getInformalNotificationByIun("IUN_3")).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId("CMP_2", "PA_2")).thenReturn(campaign);
        when(timelineUtils.buildInformalNotificationViewedTimelineElement(
                eq(notification),
                eq(0),
                eq(viewedTimelineId),
                any(Instant.class),
                eq("IO"),
                eq(null)
        )).thenReturn(TimelineElementInternal.builder().elementId(viewedTimelineId).build());

        handler.handleViewNotification(payload);

        verify(auditLogService).buildAuditLogEvent(
                eq("IUN_3"),
                eq(0),
                eq(PnAuditLogEventType.AUD_COM_VIEW_RCP),
                any(),
                eq(0)
        );
        verify(workflowUtils, never()).scheduleWorkflowDone(anyString(), anyInt(), anyString(), any());
    }
}

