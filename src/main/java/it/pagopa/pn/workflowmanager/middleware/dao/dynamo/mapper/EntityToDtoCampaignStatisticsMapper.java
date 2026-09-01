package it.pagopa.pn.workflowmanager.middleware.dao.dynamo.mapper;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import org.springframework.stereotype.Component;

@Component
public class EntityToDtoCampaignStatisticsMapper {

    public static CampaignStatisticsResponse entityToDto(CampaignStatisticsEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CampaignStatisticsResponse()
                .campaignId(entity.getCampaignId())
                .stats(new CampaignStats()
                        .totalCount(checkValue(entity.getTotalAccepted()))
                        .totalRefusedCount(checkValue(entity.getTotalRefused()))
                        .sentOnChannelCount(checkValue(entity.getTotalSent()))
                        .deliveredCount(checkValue(entity.getTotalDelivered()))
                        .undeliverableCount(checkValue(entity.getTotalUndeliverable()))
                        .workflowDoneCount(checkValue(entity.getWorkflowDone()))
                        .viewedCount(checkValue(entity.getViewedIO()) + checkValue(entity.getViewedSEND()))
                        .paidCount(checkValue(entity.getPaid()))
                        .sentOnChannel(mapSentOnChannel(entity))
                        .delivered(mapDelivered(entity))
                        .viewed(mapViewed(entity)))
                .lastCompletedTimestamp(entity.getLastCompletedTimestamp());
    }

    private static CampaignStatsSentOnChannel mapSentOnChannel(CampaignStatisticsEntity entity) {
        return new CampaignStatsSentOnChannel()
                .digital(new CampaignStatsSentOnChannelDigital()
                        .IO(checkValue(entity.getDigitalSentIO()))
                        .EMAIL(checkValue(entity.getDigitalSentEMAIL()))
                        .PEC(checkValue(entity.getDigitalSentPEC()))
                        .SMS(checkValue(entity.getDigitalSentSMS())))
                .analog(new CampaignStatsSentOnChannelAnalog()
                        .RS(checkValue(entity.getAnalogSentRS())));
    }

    private static CampaignStatsDelivered mapDelivered(CampaignStatisticsEntity entity) {
        return new CampaignStatsDelivered()
                .IO(checkValue(entity.getReceivedIO()))
                .EMAIL(checkValue(entity.getReceivedEMAIL()))
                .PEC(checkValue(entity.getReceivedPEC()))
                .SMS(checkValue(entity.getReceivedSMS()))
                .RS(checkValue(entity.getReceivedRS()));
    }

    private static CampaignStatsViewed mapViewed(CampaignStatisticsEntity entity) {
        return new CampaignStatsViewed()
                .IO(checkValue(entity.getViewedIO()))
                .SEND(checkValue(entity.getViewedSEND()));
    }

    private static int checkValue(Integer value) {
        return value != null ? value : 0;
    }
}