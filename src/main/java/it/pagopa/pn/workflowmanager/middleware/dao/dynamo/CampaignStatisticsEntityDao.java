package it.pagopa.pn.workflowmanager.middleware.dao.dynamo;

import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;

import java.util.Optional;

public interface CampaignStatisticsEntityDao {

    Optional<CampaignStatisticsEntity> get(String senderId, String campaignId);

    CampaignStatisticsEntity save(CampaignStatisticsEntity entity);

}
