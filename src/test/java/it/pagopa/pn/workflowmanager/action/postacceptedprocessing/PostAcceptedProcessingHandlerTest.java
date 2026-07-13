package it.pagopa.pn.workflowmanager.action.postacceptedprocessing;

import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAcceptedProcessingHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private CampaignService campaignService;
    @Mock
    private WorkflowUtils workflowUtils;

    private PostAcceptedProcessingHandler handler;

    private static final String TEST_IUN = "IUN-001";
    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_PA_ID = "PA-001";

    @BeforeEach
    void setUp() {
        handler = new PostAcceptedProcessingHandler(notificationService, campaignService, workflowUtils);
    }

    @Test
    void handleShouldScheduleStartWorkflowForFirstChannelOfRecipientType() {
        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(NotificationSenderInt.builder().paId(TEST_PA_ID).build())
                .recipients(List.of(
                        NotificationRecipientInt.builder().recipientType(RecipientTypeInt.PF).build()
                ))
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .workflow(List.of(
                        WorkFlowEntity.builder().channel(ChannelType.EMAIL).recipientType(Set.of(RecipientTypeInt.PG)).build(),
                        WorkFlowEntity.builder().channel(ChannelType.IO).recipientType(Set.of(RecipientTypeInt.PF)).build(),
                        WorkFlowEntity.builder().channel(ChannelType.SMS).recipientType(Set.of(RecipientTypeInt.PF)).build()
                ))
                .build();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        handler.handle(TEST_IUN);

        verify(workflowUtils).scheduleStartWorkflow(TEST_IUN, 0, 0, ChannelType.IO);
    }

    @Test
    void handleShouldScheduleForEachRecipient() {
        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(NotificationSenderInt.builder().paId(TEST_PA_ID).build())
                .recipients(List.of(
                        NotificationRecipientInt.builder().recipientType(RecipientTypeInt.PF).build(),
                        NotificationRecipientInt.builder().recipientType(RecipientTypeInt.PG).build()
                ))
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .workflow(List.of(
                        WorkFlowEntity.builder().channel(ChannelType.IO).recipientType(Set.of(RecipientTypeInt.PF)).build(),
                        WorkFlowEntity.builder().channel(ChannelType.PEC).recipientType(Set.of(RecipientTypeInt.PG)).build()
                ))
                .build();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        handler.handle(TEST_IUN);

        verify(workflowUtils).scheduleStartWorkflow(TEST_IUN, 0, 0, ChannelType.IO);
        verify(workflowUtils).scheduleStartWorkflow(TEST_IUN, 1, 0, ChannelType.PEC);
    }

    @Test
    void handleShouldThrowWhenNoWorkflowStepExistsForRecipientType() {
        NotificationInt notification = NotificationInt.builder()
                .iun(TEST_IUN)
                .campaignId(TEST_CAMPAIGN_ID)
                .sender(NotificationSenderInt.builder().paId(TEST_PA_ID).build())
                .recipients(List.of(
                        NotificationRecipientInt.builder().recipientType(RecipientTypeInt.PF).build()
                ))
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .workflow(List.of(
                        WorkFlowEntity.builder().channel(ChannelType.PEC).recipientType(Set.of(RecipientTypeInt.PG)).build()
                ))
                .build();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(campaignService.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_PA_ID)).thenReturn(campaign);

        assertThrows(PnWorkflowException.class, () -> handler.handle(TEST_IUN));
    }
}

