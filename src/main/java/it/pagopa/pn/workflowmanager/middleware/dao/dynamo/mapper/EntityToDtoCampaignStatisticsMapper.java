package it.pagopa.pn.workflowmanager.middleware.dao.dynamo.mapper;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;

import java.time.Instant;

public class EntityToDtoCampaignStatisticsMapper {

    public static CampaignStatisticsResponse entityToDto(CampaignStatisticsEntity entity ) {
        CampaignStatisticsResponse campaignStatisticsResponse = new CampaignStatisticsResponse();
        CampaignStatisticsResponseCounters counters = new CampaignStatisticsResponseCounters();
        CampaignStatisticsResponseCountersSent countersSent = getCampaignStatisticsResponseCountersSent(entity);
        CampaignStatisticsResponseCountersReceived countersReceived = getCampaignStatisticsResponseCountersReceived(entity);
        counters.setSent(countersSent);
        counters.setReceived(countersReceived);

        counters.setWorkflowDone(entity.getWorkflowDone());
        counters.setTotalReached(entity.getTotalReached());
        counters.setViewed(entity.getViewed());
        counters.setPayed(entity.getPayed());
        counters.setUndeliverable(entity.getTotalUndeliverable());

        campaignStatisticsResponse.setCampaignId(entity.getCampaignId());
        campaignStatisticsResponse.setCounters(counters);

        campaignStatisticsResponse.setLastCompletedTimestamp(
                entity.getLastCompletedTimestamp() != null
                        ? Instant.parse(entity.getLastCompletedTimestamp())
                        : null
        );

        return campaignStatisticsResponse;
    }

    private static CampaignStatisticsResponseCountersReceived getCampaignStatisticsResponseCountersReceived(CampaignStatisticsEntity entity) {
        CampaignStatisticsResponseCountersReceived countersReceived = new CampaignStatisticsResponseCountersReceived();
        countersReceived.setEMAIL(entity.getReceivedEMAIL());
        countersReceived.setIO(entity.getReceivedIO());
        countersReceived.setPEC(entity.getReceivedPEC());
        countersReceived.setRS(entity.getReceivedRS());
        return countersReceived;
    }

    private static CampaignStatisticsResponseCountersSent getCampaignStatisticsResponseCountersSent(CampaignStatisticsEntity entity) {
        CampaignStatisticsResponseCountersSent countersSent = new CampaignStatisticsResponseCountersSent();
        CampaignStatisticsResponseCountersSentAnalog countersSentAnalog = new CampaignStatisticsResponseCountersSentAnalog();
        CampaignStatisticsResponseCountersSentDigital countersSentDigital = new CampaignStatisticsResponseCountersSentDigital();
        countersSentDigital.setEMAIL(entity.getDigitalSentEMAIL());
        countersSentDigital.setIO(entity.getDigitalSentIO());
        countersSentDigital.setPEC(entity.getDigitalSentPEC());
        countersSentDigital.setSMS(entity.getDigitalSentSMS());
        countersSentAnalog.setRS(entity.getAnalogSentRS());

        countersSent.setTotal(entity.getTotalSent());
        countersSent.setAnalog(countersSentAnalog);
        countersSent.setRefused(entity.getTotalRefused());
        countersSent.setDigital(countersSentDigital);
        return countersSent;
    }

}