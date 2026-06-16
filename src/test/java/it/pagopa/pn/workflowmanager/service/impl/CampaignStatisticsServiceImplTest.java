package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class CampaignStatisticsServiceImplTest {
    Duration d = Duration.ofMillis(3000);

    private CampaignStatisticsEntityDao dao;
    private CampaignStatisticsServiceImpl service;

    private final int maxStreams = 5;

    @BeforeEach
    void setup() {
        dao = Mockito.mock( CampaignStatisticsEntityDao.class );
        service = new CampaignStatisticsServiceImpl(dao);
    }

    @Test
    void getEventStream() {
        //GIVEN

        UUID uuidd = UUID.randomUUID();
        String uuid = uuidd.toString();
        CampaignStatisticsEntity entity = new CampaignStatisticsEntity();
        entity.setCampaignId(uuid);

        when(dao.get(uuid)).thenReturn(Mono.just(entity));

        //WHEN
        it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse res = service.getCampaignStatistics(uuid).block(d);

        //THEN
        assertNotNull(res);
        Mockito.verify(dao).get(uuid);
    }

}
