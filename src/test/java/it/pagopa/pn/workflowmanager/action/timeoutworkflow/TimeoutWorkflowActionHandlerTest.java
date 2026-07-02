package it.pagopa.pn.workflowmanager.action.timeoutworkflow;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutWorkflowActionHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private WorkflowUtils workflowUtils;

    @Mock
    private SchedulerService schedulerService;

    private TimeoutWorkflowActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";
    private static final ChannelType TEST_CHANNEL_DIGITAL = ChannelType.IO;
    private static final ChannelType TEST_CHANNEL_ANALOG = ChannelType.ANALOG;
    private static final int TEST_STEP_INDEX = 1;

    @BeforeEach
    void setup() {
        handler = new TimeoutWorkflowActionHandler(
                notificationService,
                campaignService,
                workflowUtils,
                schedulerService
        );
    }

    @Test
    void timeoutWorkflowAction_shouldScheduleStartWorkflow_whenNextChannelExists() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        WorkflowUtils.NextChannel nextChannel = new WorkflowUtils.NextChannel(TEST_CHANNEL_ANALOG, TEST_STEP_INDEX);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.of(nextChannel));

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(workflowUtils).getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF);
        
        ArgumentCaptor<StartWorkflowDetails> startDetailsCaptor = ArgumentCaptor.forClass(StartWorkflowDetails.class);
        verify(schedulerService).scheduleEvent(eq(TEST_IUN), eq(TEST_REC_INDEX), any(Instant.class), 
                eq(ActionType.START_WORKFLOW), startDetailsCaptor.capture());
        
        StartWorkflowDetails capturedDetails = startDetailsCaptor.getValue();
        assertEquals(TEST_CHANNEL_ANALOG, capturedDetails.getChannel());
        assertEquals(TEST_STEP_INDEX, capturedDetails.getStepIdx());
    }

    @Test
    void timeoutWorkflowAction_shouldScheduleEndWorkflow_whenNoNextChannelExists() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(workflowUtils).getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF);
        verify(schedulerService).scheduleEvent(eq(TEST_IUN), eq(TEST_REC_INDEX), any(Instant.class), eq(ActionType.END_WORKFLOW));
    }

    @Test
    void timeoutWorkflowAction_shouldRetrieveNotificationByIun() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(any(), any(ChannelType.class), any())).thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<String> iunCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).getInformalNotificationByIun(iunCaptor.capture());
        assertEquals(TEST_IUN, iunCaptor.getValue());
    }

    @Test
    void timeoutWorkflowAction_shouldRetrieveCampaignWithCorrectIds() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<String> campaignIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignIdCaptor.capture(), paIdCaptor.capture());
        assertEquals(TEST_CAMPAIGN_ID, campaignIdCaptor.getValue());
        assertEquals(TEST_PA_ID, paIdCaptor.getValue());
    }

    @Test
    void timeoutWorkflowAction_shouldCallWorkflowUtilsWithCorrectParameters() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
        ArgumentCaptor<ChannelType> channelCaptor = ArgumentCaptor.forClass(ChannelType.class);
        ArgumentCaptor<RecipientTypeInt> recipientTypeCaptor = ArgumentCaptor.forClass(RecipientTypeInt.class);
        
        verify(workflowUtils).getNextChannel(campaignCaptor.capture(), channelCaptor.capture(), recipientTypeCaptor.capture());
        assertEquals(campaign, campaignCaptor.getValue());
        assertEquals(TEST_CHANNEL_DIGITAL, channelCaptor.getValue());
        assertEquals(RecipientTypeInt.PF, recipientTypeCaptor.getValue());
    }

    @Test
    void timeoutWorkflowAction_shouldHandlePgRecipientType() {
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
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);

        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PG))
                .thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<RecipientTypeInt> recipientTypeCaptor = ArgumentCaptor.forClass(RecipientTypeInt.class);
        verify(workflowUtils).getNextChannel(any(), any(), recipientTypeCaptor.capture());
        assertEquals(RecipientTypeInt.PG, recipientTypeCaptor.getValue());
    }

    @Test
    void timeoutWorkflowAction_shouldScheduleEventWithInstantNow() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(any(), any(), any())).thenReturn(Optional.empty());

        Instant before = Instant.now();
        
        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        Instant after = Instant.now();

        // Assert
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(schedulerService).scheduleEvent(eq(TEST_IUN), eq(TEST_REC_INDEX), instantCaptor.capture(), 
                eq(ActionType.END_WORKFLOW));
        
        Instant capturedInstant = instantCaptor.getValue();
        assertNotNull(capturedInstant);
        assertTrue(!capturedInstant.isBefore(before) && !capturedInstant.isAfter(after));
    }

    @Test
    void timeoutWorkflowAction_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);

        // Create notification with multiple recipients to support recIndex2
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
        WorkflowUtils.NextChannel nextChannel = new WorkflowUtils.NextChannel(TEST_CHANNEL_ANALOG, TEST_STEP_INDEX);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(any(), any(), any())).thenReturn(Optional.of(nextChannel));

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, recIndex2, details);

        // Assert
        ArgumentCaptor<Integer> recIndexCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(schedulerService).scheduleEvent(eq(TEST_IUN), recIndexCaptor.capture(), any(Instant.class),
                eq(ActionType.START_WORKFLOW), any(StartWorkflowDetails.class));
        assertEquals(recIndex2, recIndexCaptor.getValue());
    }

    @Test
    void timeoutWorkflowAction_shouldHandleDifferentChannels() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_ANALOG);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_ANALOG, RecipientTypeInt.PF))
                .thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(workflowUtils).getNextChannel(campaign, TEST_CHANNEL_ANALOG, RecipientTypeInt.PF);
    }

    @Test
    void timeoutWorkflowAction_shouldPassCorrectStepIndexInStartWorkflowDetails() {
        // Arrange
        int customStepIndex = 5;
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        WorkflowUtils.NextChannel nextChannel = new WorkflowUtils.NextChannel(TEST_CHANNEL_ANALOG, customStepIndex);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.of(nextChannel));

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<StartWorkflowDetails> detailsCaptor = ArgumentCaptor.forClass(StartWorkflowDetails.class);
        verify(schedulerService).scheduleEvent(anyString(), anyInt(), any(Instant.class), 
                eq(ActionType.START_WORKFLOW), detailsCaptor.capture());
        assertEquals(customStepIndex, detailsCaptor.getValue().getStepIdx());
    }

    @Test
    void timeoutWorkflowAction_shouldNotScheduleEndWorkflow_whenNextChannelIsPresent() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();
        WorkflowUtils.NextChannel nextChannel = new WorkflowUtils.NextChannel(TEST_CHANNEL_ANALOG, TEST_STEP_INDEX);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.of(nextChannel));

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(schedulerService, never()).scheduleEvent(anyString(), anyInt(), any(Instant.class), eq(ActionType.END_WORKFLOW));
        verify(schedulerService).scheduleEvent(anyString(), anyInt(), any(Instant.class), eq(ActionType.START_WORKFLOW), any());
    }

    @Test
    void timeoutWorkflowAction_shouldNotScheduleStartWorkflow_whenNoNextChannel() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);
        when(workflowUtils.getNextChannel(campaign, TEST_CHANNEL_DIGITAL, RecipientTypeInt.PF))
                .thenReturn(Optional.empty());

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(schedulerService, never()).scheduleEvent(anyString(), anyInt(), any(Instant.class), 
                eq(ActionType.START_WORKFLOW), any(StartWorkflowDetails.class));
        verify(schedulerService).scheduleEvent(anyString(), anyInt(), any(Instant.class), eq(ActionType.END_WORKFLOW));
    }

    private TimeoutWorkflowDetails createTimeoutWorkflowDetails(ChannelType channel) {
        return TimeoutWorkflowDetails.builder()
                .channel(channel)
                .build();
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
}
