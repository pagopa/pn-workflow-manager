package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import it.pagopa.pn.commons.db.campaign.entity.*;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.qr.models.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignServiceCachedProvider campaignServiceCachedProvider;

    private CampaignServiceImpl service;

    private static final String TEST_CAMPAIGN_ID = "CAMPAIGN-001";
    private static final String TEST_SENDER_ID = "12345678-1234-1234-1234-123456789012";
    private static final String TEST_TITLE = "Test Campaign";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_SERVICE_ID = "SERVICE-001";

    @BeforeEach
    void setup() {
        service = new CampaignServiceImpl(campaignServiceCachedProvider);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldReturnCampaign_whenCampaignExists() {
        // Arrange
        CampaignEntity campaignEntity = createMockCampaignEntity();
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaignEntity);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CAMPAIGN_ID, result.getCampaignId());
        assertEquals(TEST_SENDER_ID, result.getSenderId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_DESCRIPTION, result.getDescriptionScope());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldCallProviderWithCorrectParameters() {
        // Arrange
        CampaignEntity campaignEntity = createMockCampaignEntity();
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaignEntity);

        // Act
        service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);

        // Assert
        verify(campaignServiceCachedProvider, times(1)).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldHandleDifferentCampaignIds() {
        // Arrange
        String differentCampaignId = "CAMPAIGN-999";
        CampaignEntity campaignEntity = createMockCampaignEntity();
        campaignEntity.setCampaignId(differentCampaignId);
        
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID))
                .thenReturn(campaignEntity);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(differentCampaignId, result.getCampaignId());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(differentCampaignId, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldHandleDifferentSenderIds() {
        // Arrange
        String differentSenderId = "87654321-4321-4321-4321-210987654321";
        CampaignEntity campaignEntity = createMockCampaignEntity();
        campaignEntity.setSenderId(differentSenderId);
        
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId))
                .thenReturn(campaignEntity);

        // Act
        Campaign result = service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId);

        // Assert
        assertNotNull(result);
        assertEquals(differentSenderId, result.getSenderId());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, differentSenderId);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldReturnCampaignWithAllFields() {
        // Arrange
        CampaignEntity campaignEntity = createCompletelyPopulatedCampaignEntity();
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaignEntity);

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
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldThrowException_whenProviderThrowsException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Provider error");
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(anyString(), anyString()))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, 
                () -> service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID));
        
        assertEquals("Provider error", thrownException.getMessage());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    @Test
    void getCampaignByCampaignIdAndSenderId_shouldThrowException_whenCampaignStatusIsNotInProgress() {
        // Arrange
        CampaignEntity campaignEntity = createMockCampaignEntity();
        campaignEntity.setStatus(CampaignStatus.DRAFT);
        
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID))
                .thenReturn(campaignEntity);

        // Act & Assert
        PnInternalException thrownException = assertThrows(PnInternalException.class,
                () -> service.getCampaignByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID));
        
        assertNotNull(thrownException.getProblem().getDetail());
        assertTrue(thrownException.getProblem().getDetail().contains("Campaign CAMPAIGN-001 has DRAFT status"));
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(TEST_CAMPAIGN_ID, TEST_SENDER_ID);
    }

    private CampaignEntity createMockCampaignEntity() {
        return CampaignEntity.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .senderId(TEST_SENDER_ID)
                .title(TEST_TITLE)
                .descriptionScope(TEST_DESCRIPTION)
                .serviceId(TEST_SERVICE_ID)
                .status(CampaignStatus.IN_PROGRESS)
                .startDate(Instant.now())
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .workflow(new ArrayList<>())
                .build();
    }

    private CampaignEntity createCompletelyPopulatedCampaignEntity() {
        Set<RecipientTypeInt> recipientTypes = new HashSet<>();
        recipientTypes.add(RecipientTypeInt.PF);
        recipientTypes.add(RecipientTypeInt.PG);

        Set<DesiredFeedback> desiredFeedbacks = new HashSet<>();
        desiredFeedbacks.add(DesiredFeedback.SENT);

        List<WorkflowEntity> workflow = new ArrayList<>();
        WorkflowEntity step = WorkflowEntity.builder()
                .channel(CampaignChannel.EMAIL)
                .recipientType(recipientTypes)
                .timeout(Duration.ofHours(24))
                .includeAttachment(true)
                .desiredFeedback(desiredFeedbacks)
                .build();
        workflow.add(step);

        return CampaignEntity.builder()
                .campaignId(TEST_CAMPAIGN_ID)
                .senderId(TEST_SENDER_ID)
                .title(TEST_TITLE)
                .descriptionScope(TEST_DESCRIPTION)
                .serviceId(TEST_SERVICE_ID)
                .startDate(Instant.now())
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(CampaignStatus.IN_PROGRESS)
                .senderContact("contact@example.com")
                .sensitiveContent(false)
                .stopOnViewed(true)
                .workflow(workflow)
                .build();
    }
}
