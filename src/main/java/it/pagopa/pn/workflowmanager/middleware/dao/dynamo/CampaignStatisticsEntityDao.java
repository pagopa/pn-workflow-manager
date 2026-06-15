package it.pagopa.pn.workflowmanager.middleware.dao.dynamo;

import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import reactor.core.publisher.Mono;

public interface CampaignStatisticsEntityDao {

    Mono<CampaignStatisticsEntity> get(String campaignId);

}
