package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.exceptions.PnCampaignStatisticsNotFoundException;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CampaignStatisticsServiceImplTest {
    Duration d = Duration.ofMillis(3000);

    private CampaignStatisticsEntityDao dao;
    private CampaignStatisticsServiceImpl service;

    @BeforeEach
    void setup() {
        dao = Mockito.mock( CampaignStatisticsEntityDao.class );
        service = new CampaignStatisticsServiceImpl(dao);
    }

    @Test
    void getCampaignStatistics() {
        //GIVEN

        UUID uuidd = UUID.randomUUID();
        String uuid = uuidd.toString();
        CampaignStatisticsEntity entity = new CampaignStatisticsEntity();
        entity.setCampaignId(uuid);
        entity.setLastCompletedTimestamp("2026-01-21T14:46:43.158602555Z");

        when(dao.get(uuid)).thenReturn(Mono.just(entity));

        //WHEN
        it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse res = service.getCampaignStatistics(uuid).block(d);

        //THEN
        assertNotNull(res);
        assertEquals(Instant.parse("2026-01-21T14:46:43.158602555Z"), res.getLastCompletedTimestamp());
        Mockito.verify(dao).get(uuid);
    }

    @Test
    void getCampaignStatisticsNotFound() {
        // GIVEN
        String campaignId = UUID.randomUUID().toString();
        when(dao.get(campaignId)).thenReturn(Mono.empty());

        // WHEN - THEN
        assertThrows(PnCampaignStatisticsNotFoundException.class,
                () -> service.getCampaignStatistics(campaignId).block(d));

        Mockito.verify(dao).get(campaignId);
    }

}
