package it.pagopa.pn.workflowmanager.action.timeoutworkflow;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
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
class TimeoutWorkflowActionHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private WorkflowUtils workflowUtils;

    private TimeoutWorkflowActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";
    private static final ChannelType TEST_CHANNEL_DIGITAL = ChannelType.IO;

    @BeforeEach
    void setup() {
        handler = new TimeoutWorkflowActionHandler(
                notificationService,
                campaignService,
                workflowUtils
        );
    }

    @Test
    void timeoutWorkflowAction_shouldAlwaysAdvanceWorkflow() {
        // Arrange
        TimeoutWorkflowDetails details = createTimeoutWorkflowDetails();
        NotificationInt notification = createMockNotification();
        Campaign campaign = createMockCampaign();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        // Act
        handler.timeoutWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(notificationService).getInformalNotificationByIun(TEST_IUN);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID);
        verify(workflowUtils).advanceWorkflow(TEST_IUN, TEST_REC_INDEX, TEST_CHANNEL_DIGITAL, campaign, RecipientTypeInt.PF);
    }


    private TimeoutWorkflowDetails createTimeoutWorkflowDetails() {
        return TimeoutWorkflowDetails.builder()
                .channel(TEST_CHANNEL_DIGITAL)
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
