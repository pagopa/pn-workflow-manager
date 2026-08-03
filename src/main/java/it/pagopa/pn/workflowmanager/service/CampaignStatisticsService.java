package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import reactor.core.publisher.Mono;

public interface CampaignStatisticsService {

    Mono<CampaignStatisticsResponse> getCampaignStatistics(String campaignId);
}
