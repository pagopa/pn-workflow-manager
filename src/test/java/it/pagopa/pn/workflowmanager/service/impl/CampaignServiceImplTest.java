package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.CampaignsParameterConsumer;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignsParameterConsumer campaignsParameterConsumer;

    private CampaignServiceImpl service;

    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_SENDER_ID = "SENDER-001";
    private static final String TEST_TITLE = "Test Campaign";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_SERVICE_ID = "SERVICE-001";

    @BeforeEach
    void setup() {
        service = new CampaignServiceImpl(campaignsParameterConsumer);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldReturnCampaign_whenCampaignExists() {
        // Arrange
        Campaign expectedCampaign = createMockCampaign();
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(expectedCampaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CAMPAIGN_ID, result.getCampaignId());
        assertEquals(TEST_SENDER_ID, result.getSenderId());
        assertEquals(TEST_TITLE, result.getTitle());
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldCallConsumerWithCorrectParameters() {
        // Arrange
        Campaign campaign = createMockCampaign();
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaign);

        // Act
        service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        verify(campaignsParameterConsumer, times(1)).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldHandleDifferentCampaignIds() {
        // Arrange
        String differentCampaignId = "CAMPAIGN-999";
        Campaign campaign = createMockCampaign();
        campaign.setCampaignId(differentCampaignId);
        
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID))
                .thenReturn(campaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(differentCampaignId, result.getCampaignId());
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldHandleDifferentSenderIds() {
        // Arrange
        String differentSenderId = "SENDER-999";
        Campaign campaign = createMockCampaign();
        campaign.setSenderId(differentSenderId);
        
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId))
                .thenReturn(campaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId);

        // Assert
        assertNotNull(result);
        assertEquals(differentSenderId, result.getSenderId());
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldReturnCampaignWithAllFields() {
        // Arrange
        Campaign campaign = createCompletelyPopulatedCampaign();
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CAMPAIGN_ID, result.getCampaignId());
        assertEquals(TEST_SENDER_ID, result.getSenderId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_DESCRIPTION, result.getDescriptionScope());
        assertEquals(TEST_SERVICE_ID, result.getServiceId());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
        assertNotNull(result.getStatus());
        assertNotNull(result.getSensitiveContent());
        assertNotNull(result.getStopOnViewed());
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldThrowException_whenConsumerThrowsException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Consumer error");
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(anyString(), anyString()))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, 
                () -> service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID));
        
        assertEquals("Consumer error", thrownException.getMessage());
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldReturnSameCampaignObjectFromConsumer() {
        // Arrange
        Campaign campaign = createMockCampaign();
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        assertSame(campaign, result);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldDelegateDirectlyToConsumer() {
        // Arrange
        Campaign campaign = createMockCampaign();
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaign);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        assertEquals(campaign, result);
        verify(campaignsParameterConsumer, times(1)).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
        verifyNoMoreInteractions(campaignsParameterConsumer);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldThrowException_whenCampaignStatusIsNotInProgress() {

        Campaign campaign = createMockCampaign();
        campaign.setStatus(CampaignStatus.DRAFT);
        when(campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaign);

        PnInternalException thrownException = assertThrows(PnInternalException.class,
                () -> service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID));

        assertTrue(thrownException.getProblem().getDetail().contains("Campaign CAMPAIGN-001 has DRAFT status"));
        verify(campaignsParameterConsumer).getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    private Campaign createMockCampaign() {
        return Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .senderId(TEST_SENDER_ID)
                .title(TEST_TITLE)
                .descriptionScope(TEST_DESCRIPTION)
                .serviceId(TEST_SERVICE_ID)
                .status(CampaignStatus.IN_PROGRESS)
                .build();
    }

    private Campaign createCompletelyPopulatedCampaign() {
        return Campaign.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .senderId(TEST_SENDER_ID)
                .title(TEST_TITLE)
                .descriptionScope(TEST_DESCRIPTION)
                .serviceId(TEST_SERVICE_ID)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(30))
                .status(CampaignStatus.IN_PROGRESS)
                .senderContact("contact@example.com")
                .sensitiveContent(false)
                .stopOnViewed(true)
                .workflow(new ArrayList<>())
                .build();
    }
}
