package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;

public interface CampaignStatisticsService {

    CampaignStatisticsResponse getCampaignStatistics(String xPagopaPnCxId,String campaignId);
}
