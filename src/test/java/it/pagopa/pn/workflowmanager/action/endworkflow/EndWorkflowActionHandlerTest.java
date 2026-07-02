package it.pagopa.pn.workflowmanager.action.endworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndWorkflowActionHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private RecipientDeliveryAnalyzer recipientDeliveryAnalyzer;

    @Mock
    private TimelineService timelineService;

    private EndWorkflowActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_TIMELINE_ID = "TIMELINE-001";
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";

    @BeforeEach
    void setup() {
        handler = new EndWorkflowActionHandler(
                notificationService,
                campaignService,
                timelineUtils,
                recipientDeliveryAnalyzer,
                timelineService
        );
    }

    @Test
    void endWorkflowAction_shouldCreateReachedTimelineElement_whenRecipientIsReached() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void endWorkflowAction_shouldCreateUnreachedTimelineElement_whenRecipientIsUnreached() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_UNREACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.UNREACHED);
        when(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void endWorkflowAction_shouldCreateUndeliverableTimelineElement_whenRecipientIsUndeliverable() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_UNDELIVERABLE);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.UNDELIVERABLE);
        when(timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString())).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedUndeliverableTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString());
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void endWorkflowAction_shouldHandlePgRecipientType() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PG);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PG))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<RecipientTypeInt> recipientTypeCaptor = ArgumentCaptor.forClass(RecipientTypeInt.class);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                recipientTypeCaptor.capture());
        assertEquals(RecipientTypeInt.PG, recipientTypeCaptor.getValue());
    }

    @Test
    void endWorkflowAction_shouldPassTimelineElementsToDeliveryAnalyzer() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        TimelineElementInternal element1 = createMockTimelineElement(SEND_DIGITAL_MESSAGE_FEEDBACK);
        TimelineElementInternal element2 = createMockTimelineElement(DELIVERED);
        List<TimelineElementInternal> timelineElements = List.of(element1, element2);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<List<TimelineElementInternal>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(listCaptor.capture(), eq(campaign),
                eq(TEST_REC_INDEX), eq(RecipientTypeInt.PF));
        assertEquals(2, listCaptor.getValue().size());
        assertEquals(timelineElements, listCaptor.getValue());
    }

    @Test
    void endWorkflowAction_shouldRetrieveCorrectCampaign_withNotificationCampaignIdAndSenderId() {
        // Arrange
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<String> campaignIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignIdCaptor.capture(), paIdCaptor.capture());
        assertEquals(TEST_CAMPAIGN_ID, campaignIdCaptor.getValue());
        assertEquals(TEST_PA_ID, paIdCaptor.getValue());
    }

    @Test
    void endWorkflowAction_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(TEST_PA_ID)
                .build();

        NotificationRecipientInt recipient0 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .build();

        NotificationRecipientInt recipient1 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .build();

        NotificationRecipientInt recipient2 = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(sender)
                .recipients(List.of(recipient0, recipient1, recipient2))
                .build();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_UNREACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(recIndex2),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.UNREACHED);
        when(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(eq(recIndex2), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, recIndex2, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<Integer> recIndexCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), recIndexCaptor.capture(),
                eq(RecipientTypeInt.PF));
        assertEquals(recIndex2, recIndexCaptor.getValue());
    }

    @Test
    void endWorkflowAction_shouldUseSourceTimelineId_forReachedAndUnreachedOnly() {
        // Arrange - Test REACHED status
        NotificationInt notification = createMockNotification(RecipientTypeInt.PF);
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert - Verify sourceTimelineId is passed for REACHED
        verify(timelineUtils).buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID));
    }

    private NotificationInt createMockNotification(RecipientTypeInt recipientType) {
        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(TEST_PA_ID)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(recipientType)
                .build();

        return NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(sender)
                .recipients(List.of(recipient))
                .build();
    }

    private Campaign createMockCampaign() {
        return Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .build();
    }

    private TimelineElementInternal createMockTimelineElement(TimelineElementCategoryInt category) {
        return TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(category)
                .build();
    }
}