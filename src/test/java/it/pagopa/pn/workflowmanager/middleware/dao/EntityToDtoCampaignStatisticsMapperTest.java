package it.pagopa.pn.workflowmanager.middleware.dao;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.mapper.EntityToDtoCampaignStatisticsMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EntityToDtoCampaignStatisticsMapperTest {

    @Test
    void shouldMapEntityToDtoCorrectly() {
        // Given
        CampaignStatisticsEntity entity = new CampaignStatisticsEntity();
        entity.setCampaignId("CAMP-12345-2026");
        entity.setTotalAccepted(150500);
        entity.setTotalRefused(45);
        entity.setTotalSent(145500);
        entity.setTotalDelivered(113210);
        entity.setTotalUndeliverable(1000);
        entity.setWorkflowDone(113210);
        entity.setPaid(1000);

        entity.setDigitalSentIO(85000);
        entity.setDigitalSentEMAIL(40000);
        entity.setDigitalSentPEC(10000);
        entity.setDigitalSentSMS(15000);
        entity.setAnalogSentRS(500);

        entity.setReceivedIO(72000);
        entity.setReceivedEMAIL(31000);
        entity.setReceivedPEC(9800);
        entity.setReceivedSMS(10);
        entity.setReceivedRS(410);

        entity.setViewedIO(600);
        entity.setViewedSEND(400);

        entity.setLastCompletedTimestamp(Instant.now());

        // When
        CampaignStatisticsResponse dto = EntityToDtoCampaignStatisticsMapper.entityToDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCampaignId()).isEqualTo("CAMP-12345-2026");
        assertThat(dto.getLastCompletedTimestamp()).isEqualTo(entity.getLastCompletedTimestamp());

        assertThat(dto.getStats()).isNotNull();
        assertThat(dto.getStats().getTotalCount()).isEqualTo(150500);
        assertThat(dto.getStats().getTotalRefusedCount()).isEqualTo(45);
        assertThat(dto.getStats().getSentOnChannelCount()).isEqualTo(145500);
        assertThat(dto.getStats().getDeliveredCount()).isEqualTo(113210);
        assertThat(dto.getStats().getUndeliverableCount()).isEqualTo(1000);
        assertThat(dto.getStats().getWorkflowDoneCount()).isEqualTo(113210);
        assertThat(dto.getStats().getPaidCount()).isEqualTo(1000);

        // Calcolo totale viewed (600 + 400)
        assertThat(dto.getStats().getViewedCount()).isEqualTo(1000);

        // Canali di invio
        assertThat(dto.getStats().getSentOnChannel()).isNotNull();
        assertThat(dto.getStats().getSentOnChannel().getDigital()).isNotNull();
        assertThat(dto.getStats().getSentOnChannel().getDigital().getIO()).isEqualTo(85000);
        assertThat(dto.getStats().getSentOnChannel().getDigital().getEMAIL()).isEqualTo(40000);
        assertThat(dto.getStats().getSentOnChannel().getDigital().getPEC()).isEqualTo(10000);
        assertThat(dto.getStats().getSentOnChannel().getDigital().getSMS()).isEqualTo(15000);

        assertThat(dto.getStats().getSentOnChannel().getAnalog()).isNotNull();
        assertThat(dto.getStats().getSentOnChannel().getAnalog().getRS()).isEqualTo(500);

        // Ricevuti / Delivered
        assertThat(dto.getStats().getDelivered()).isNotNull();
        assertThat(dto.getStats().getDelivered().getIO()).isEqualTo(72000);
        assertThat(dto.getStats().getDelivered().getEMAIL()).isEqualTo(31000);
        assertThat(dto.getStats().getDelivered().getPEC()).isEqualTo(9800);
        assertThat(dto.getStats().getDelivered().getRS()).isEqualTo(410);
        assertThat(dto.getStats().getDelivered().getSMS()).isEqualTo(10);

        // Dettaglio viewed
        assertThat(dto.getStats().getViewed()).isNotNull();
        assertThat(dto.getStats().getViewed().getIO()).isEqualTo(600);
        assertThat(dto.getStats().getViewed().getSEND()).isEqualTo(400);
    }

    @Test
    void shouldHandleNullValuesInViewedCountCalculation() {
        // Given
        CampaignStatisticsEntity entity = new CampaignStatisticsEntity();
        entity.setCampaignId("CAMP-NULL-TEST");
        entity.setViewedIO(0);
        entity.setViewedSEND(150);

        // When
        CampaignStatisticsResponse dto = EntityToDtoCampaignStatisticsMapper.entityToDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getStats()).isNotNull();
        assertThat(dto.getStats().getViewedCount()).isEqualTo(150);
        assertThat(dto.getStats().getViewed().getIO()).isEqualTo(0);
        assertThat(dto.getStats().getViewed().getSEND()).isEqualTo(150);
    }
}