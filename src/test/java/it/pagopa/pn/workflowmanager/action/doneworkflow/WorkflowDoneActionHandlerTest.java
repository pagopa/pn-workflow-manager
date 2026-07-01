package it.pagopa.pn.workflowmanager.action.doneworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.exceptions.PnEventRouterException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowDoneActionHandlerTest {

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

    private WorkflowDoneActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_TIMELINE_ID = "TIMELINE-001";
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";

    @BeforeEach
    void setup() {
        handler = new WorkflowDoneActionHandler(
                notificationService,
                campaignService,
                timelineUtils,
                recipientDeliveryAnalyzer,
                timelineService
        );
    }

    @Test
    void doneWorkflowAction_shouldCreateReachedTimelineElement_whenRecipientIsReached() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(TimelineElementCategoryInt.WORKFLOW_DONE_REACHED);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowDoneReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.doneWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN));
        verify(timelineUtils).buildWorkflowDoneReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void doneWorkflowAction_shouldCreateUnreachedTimelineElement_whenRecipientIsUnreached() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN))).thenReturn(RecipientDeliveryStatus.UNREACHED);
        when(timelineUtils.buildWorkflowDoneUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.doneWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN));
        verify(timelineUtils).buildWorkflowDoneUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void doneWorkflowAction_shouldThrowException_whenRecipientIsUndeliverable() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN))).thenReturn(RecipientDeliveryStatus.UNDELIVERABLE);

        // Act & Assert
         assertThrows(PnEventRouterException.class,
                () -> handler.doneWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID));


        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN));
        verifyNoInteractions(timelineService);
        verifyNoInteractions(timelineUtils);
    }

    @Test
    void doneWorkflowAction_shouldHandlePgRecipientType() {
        // Arrange
        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(TEST_PA_ID)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PG)
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(sender)
                .recipients(List.of(recipient))
                .build();

        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(TimelineElementCategoryInt.WORKFLOW_DONE_REACHED);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PG), eq(TEST_IUN))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowDoneReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.doneWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<RecipientTypeInt> recipientTypeCaptor = ArgumentCaptor.forClass(RecipientTypeInt.class);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                recipientTypeCaptor.capture(), eq(TEST_IUN));
        assertEquals(RecipientTypeInt.PG, recipientTypeCaptor.getValue());
    }

    @Test
    void doneWorkflowAction_shouldPassEmptyTimelineElementsList_toDeliveryAnalyzer() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(TimelineElementCategoryInt.WORKFLOW_DONE_REACHED);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryStatus(anyList(), eq(campaign), eq(TEST_REC_INDEX), 
                eq(RecipientTypeInt.PF), eq(TEST_IUN))).thenReturn(RecipientDeliveryStatus.REACHED);
        when(timelineUtils.buildWorkflowDoneReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification), 
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.doneWorkflowAction(TEST_IUN, TEST_REC_INDEX, TEST_TIMELINE_ID);

        // Assert
        ArgumentCaptor<List<TimelineElementInternal>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(recipientDeliveryAnalyzer).getDeliveryStatus(listCaptor.capture(), eq(campaign), 
                eq(TEST_REC_INDEX), eq(RecipientTypeInt.PF), eq(TEST_IUN));
        assertTrue(listCaptor.getValue().isEmpty());
    }


    private NotificationInt createMockNotification() {
        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(TEST_PA_ID)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .build();

        return NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(sender)
                .recipients(List.of(recipient))
                .build();
    }

    private Campaign createMockCampaign() {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(TEST_CAMPAIGN_ID);
        return campaign;
    }

    private TimelineElementInternal createMockTimelineElement(TimelineElementCategoryInt category) {
        return TimelineElementInternal.builder()
                .iun(TEST_IUN)
                .category(category)
                .build();
    }
}
