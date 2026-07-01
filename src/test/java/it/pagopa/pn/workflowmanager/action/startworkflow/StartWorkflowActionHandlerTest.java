package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.ChannelSenderFactory;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartWorkflowActionHandlerTest {

    @Mock
    private ChannelSenderFactory channelSenderFactory;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private ChannelSender channelSender;

    private StartWorkflowActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";
    private static final ChannelType TEST_CHANNEL_DIGITAL = ChannelType.IO;
    private static final ChannelType TEST_CHANNEL_ANALOG = ChannelType.ANALOG;

    @BeforeEach
    void setup() {
        handler = new StartWorkflowActionHandler(
                channelSenderFactory,
                notificationService,
                campaignService
        );
    }

    @Test
    void startWorkflowAction_shouldSendNotificationViaDigitalChannel() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(channelSenderFactory).getChannelSender(TEST_CHANNEL_DIGITAL);
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(channelSender).send(notification, campaign, TEST_REC_INDEX, 0, TEST_CHANNEL_DIGITAL);
    }

    @Test
    void startWorkflowAction_shouldSendNotificationViaAnalogChannel() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_ANALOG);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_ANALOG)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(channelSenderFactory).getChannelSender(TEST_CHANNEL_ANALOG);
        verify(channelSender).send(notification, campaign, TEST_REC_INDEX, 0, TEST_CHANNEL_ANALOG);
    }

    @Test
    void startWorkflowAction_shouldRetrieveNotificationByIun() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(any(ChannelType.class))).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<String> iunCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).getInformalNotificationByIun(iunCaptor.capture());
        assertEquals(TEST_IUN, iunCaptor.getValue());
    }

    @Test
    void startWorkflowAction_shouldRetrieveCampaignWithCorrectIds() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(any(ChannelType.class))).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<String> campaignIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignIdCaptor.capture(), paIdCaptor.capture());
        assertEquals(TEST_CAMPAIGN_ID, campaignIdCaptor.getValue());
        assertEquals(TEST_PA_ID, paIdCaptor.getValue());
    }

    @Test
    void startWorkflowAction_shouldPassCorrectParametersToChannelSender() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<NotificationInt> notificationCaptor = ArgumentCaptor.forClass(NotificationInt.class);
        ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
        ArgumentCaptor<Integer> recIndexCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sentAttemptCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ChannelType> channelCaptor = ArgumentCaptor.forClass(ChannelType.class);

        verify(channelSender).send(
                notificationCaptor.capture(),
                campaignCaptor.capture(),
                recIndexCaptor.capture(),
                sentAttemptCaptor.capture(),
                channelCaptor.capture()
        );

        assertEquals(notification, notificationCaptor.getValue());
        assertEquals(campaign, campaignCaptor.getValue());
        assertEquals(TEST_REC_INDEX, recIndexCaptor.getValue());
        assertEquals(0, sentAttemptCaptor.getValue()); // sentAttemptMade is always 0
        assertEquals(TEST_CHANNEL_DIGITAL, channelCaptor.getValue());
    }

    @Test
    void startWorkflowAction_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, recIndex2, details);

        // Assert
        ArgumentCaptor<Integer> recIndexCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(channelSender).send(any(), any(), recIndexCaptor.capture(), anyInt(), any());
        assertEquals(recIndex2, recIndexCaptor.getValue());
    }

    @Test
    void startWorkflowAction_shouldAlwaysUseSentAttemptMadeZero() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        ArgumentCaptor<Integer> sentAttemptCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(channelSender).send(any(), any(), anyInt(), sentAttemptCaptor.capture(), any());
        assertEquals(0, sentAttemptCaptor.getValue());
    }

    @Test
    void startWorkflowAction_shouldCallServicesInCorrectOrder() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert - Verify order of invocations
        var inOrder = inOrder(channelSenderFactory, notificationService, campaignService, channelSender);
        inOrder.verify(channelSenderFactory).getChannelSender(TEST_CHANNEL_DIGITAL);
        inOrder.verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        inOrder.verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        inOrder.verify(channelSender).send(notification, campaign, TEST_REC_INDEX, 0, TEST_CHANNEL_DIGITAL);
    }

    @Test
    void startWorkflowAction_shouldUseChannelFromDetails() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails(TEST_CHANNEL_DIGITAL);
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(channelSenderFactory).getChannelSender(TEST_CHANNEL_DIGITAL);
        verify(channelSender).send(notification, campaign, TEST_REC_INDEX, 0, TEST_CHANNEL_DIGITAL);
    }


    private StartWorkflowDetails createStartWorkflowDetails(ChannelType channel) {
        StartWorkflowDetails details = new StartWorkflowDetails();
        details.setChannel(channel);
        return details;
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
}
