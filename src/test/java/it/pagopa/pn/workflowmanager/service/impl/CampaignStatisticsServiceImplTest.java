package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignNotFoundException;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignStatisticsNotFoundException;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_NOT_FOUND;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignStatisticsServiceImplTest {

    private static final String SENDER_ID = "sender-001";
    private static final String CAMPAIGN_ID = "campaign-001";

    @Mock
    private CampaignStatisticsEntityDao dao;
    @Mock
    private CampaignServiceCachedProvider campaignServiceCachedProvider;

    private CampaignStatisticsServiceImpl service;

    @BeforeEach
    void setup() {
        service = new CampaignStatisticsServiceImpl(dao, campaignServiceCachedProvider);
    }

    @Test
    void getCampaignStatistics() {
        Instant timestamp = Instant.parse("2026-06-08T19:13:00Z");
        CampaignStatisticsEntity entity = CampaignStatisticsEntity.builder()
                .senderId(SENDER_ID)
                .campaignId(CAMPAIGN_ID)
                .totalAccepted(20)
                .totalSent(15)
                .viewedIO(3)
                .viewedSEND(2)
                .lastCompletedTimestamp(timestamp)
                .build();
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.of(entity));

        CampaignStatisticsResponse response = service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID);

        assertEquals(CAMPAIGN_ID, response.getCampaignId());
        assertEquals(timestamp, response.getLastCompletedTimestamp());
        assertEquals(20, response.getStats().getTotalCount());
        assertEquals(15, response.getStats().getSentOnChannelCount());
        assertEquals(5, response.getStats().getViewedCount());
        verify(dao).get(SENDER_ID, CAMPAIGN_ID);
        verifyNoMoreInteractions(dao);
        verifyNoInteractions(campaignServiceCachedProvider);
    }

    @Test
    void getCampaignStatisticsWithEmptyCounters() {
        CampaignStatisticsEntity entity = CampaignStatisticsEntity.builder()
                .senderId(SENDER_ID)
                .campaignId(CAMPAIGN_ID)
                .build();
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.of(entity));

        CampaignStatisticsResponse response = service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID);

        assertEquals(CAMPAIGN_ID, response.getCampaignId());
        assertEquals(0, response.getStats().getTotalCount());
        assertEquals(0, response.getStats().getViewedCount());
        assertNull(response.getLastCompletedTimestamp());
        verifyNoInteractions(campaignServiceCachedProvider);
    }

    @Test
    void getCampaignStatisticsNotFoundWhenCampaignExists() {
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenReturn(CampaignEntity.builder().campaignId(CAMPAIGN_ID).senderId(SENDER_ID).build());

        PnCampaignStatisticsNotFoundException exception = assertThrows(
                PnCampaignStatisticsNotFoundException.class,
                () -> service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID));

        assertEquals(404, exception.getProblem().getStatus());
        assertEquals("Campaign statistics not found", exception.getProblem().getTitle());
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND,
                exception.getProblem().getErrors().getFirst().getCode());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);
        verify(dao).get(SENDER_ID, CAMPAIGN_ID);
        verifyNoMoreInteractions(dao);
    }

    @Test
    void getCampaignStatisticsNotFoundWhenProviderReturnsNull() {
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenReturn(null);

        PnCampaignNotFoundException exception = assertThrows(PnCampaignNotFoundException.class,
                () -> service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID));

        assertEquals(404, exception.getProblem().getStatus());
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_NOT_FOUND,
                exception.getProblem().getErrors().getFirst().getCode());
        verify(campaignServiceCachedProvider).getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID);
    }

    @Test
    void getCampaignStatisticsPropagatesProviderNotFound() {
        var expected = new it.pagopa.pn.commons.exceptions.PnCampaignNotFoundException("Campaign not found");
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenThrow(expected);

        var exception = assertThrows(it.pagopa.pn.commons.exceptions.PnCampaignNotFoundException.class,
                () -> service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID));

        assertSame(expected, exception);
        assertEquals(404, exception.getProblem().getStatus());
    }

    @Test
    void getCampaignStatisticsPropagatesDaoFailure() {
        RuntimeException expected = new IllegalStateException("DynamoDB unavailable");
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenThrow(expected);

        RuntimeException exception = assertThrows(IllegalStateException.class,
                () -> service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID));

        assertSame(expected, exception);
        verifyNoInteractions(campaignServiceCachedProvider);
    }

    @Test
    void getCampaignStatisticsPropagatesProviderFailure() {
        RuntimeException expected = new IllegalStateException("Campaign lookup failed");
        when(dao.get(SENDER_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignServiceCachedProvider.getByCampaignIdAndSenderId(CAMPAIGN_ID, SENDER_ID))
                .thenThrow(expected);

        RuntimeException exception = assertThrows(IllegalStateException.class,
                () -> service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID));

        assertSame(expected, exception);
    }
}
