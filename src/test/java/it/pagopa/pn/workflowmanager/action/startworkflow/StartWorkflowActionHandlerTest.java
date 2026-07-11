package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.workflowmanager.action.ChannelSender;
import it.pagopa.pn.workflowmanager.action.ChannelSenderFactory;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final int TEST_STEP_IDX = 0;

    @BeforeEach
    void setup() {
        handler = new StartWorkflowActionHandler(
                channelSenderFactory,
                notificationService,
                campaignService
        );
    }

    @Test
    void startWorkflowAction_shouldPassCorrectParametersToChannelSender() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(channelSender).send(
                notification,
                campaign,
                TEST_REC_INDEX,
                TEST_STEP_IDX
        );
    }


    private StartWorkflowDetails createStartWorkflowDetails() {
        StartWorkflowDetails details = new StartWorkflowDetails();
        details.setChannel(TEST_CHANNEL_DIGITAL);
        details.setStepIdx(TEST_STEP_IDX);
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
