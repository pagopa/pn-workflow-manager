package it.pagopa.pn.workflowmanager.action.endworkflow;

import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryAnalyzer;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryInfo;
import it.pagopa.pn.workflowmanager.action.utils.RecipientDeliveryStatus;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
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
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_REACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryInfo(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(new RecipientDeliveryInfo(RecipientDeliveryStatus.REACHED, TEST_TIMELINE_ID));
        when(timelineUtils.buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID))).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryInfo(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedReachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString(), eq(TEST_TIMELINE_ID));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void endWorkflowAction_shouldCreateUnreachedTimelineElement_whenRecipientIsUnreached() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_UNREACHED);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryInfo(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(new RecipientDeliveryInfo(RecipientDeliveryStatus.UNREACHED));
        when(timelineUtils.buildWorkflowEndedUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString())).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryInfo(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedUnreachedTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString());
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
    }

    @Test
    void endWorkflowAction_shouldCreateUndeliverableTimelineElement_whenRecipientIsUndeliverable() {
        // Arrange
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        TimelineElementInternal timelineElement = createMockTimelineElement(WORKFLOW_ENDED_UNDELIVERABLE);
        List<TimelineElementInternal> timelineElements = List.of();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(recipientDeliveryAnalyzer.getDeliveryInfo(anyList(), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF))).thenReturn(new RecipientDeliveryInfo(RecipientDeliveryStatus.UNDELIVERABLE));
        when(timelineUtils.buildWorkflowEndedUndeliverableTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString())).thenReturn(timelineElement);

        // Act
        handler.endWorkflowAction(timelineElements, TEST_IUN, TEST_REC_INDEX);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(recipientDeliveryAnalyzer).getDeliveryInfo(eq(timelineElements), eq(campaign), eq(TEST_REC_INDEX),
                eq(RecipientTypeInt.PF));
        verify(timelineUtils).buildWorkflowEndedUndeliverableTimelineElement(eq(TEST_REC_INDEX), eq(notification),
                anyString());
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineUtils);
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