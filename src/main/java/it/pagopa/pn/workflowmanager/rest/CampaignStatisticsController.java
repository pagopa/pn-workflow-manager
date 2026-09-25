package it.pagopa.pn.workflowmanager.rest;

import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.api.CampaignStatisticsApi;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CampaignStatisticsController implements CampaignStatisticsApi {

    private final CampaignStatisticsService campaignStatisticsService;

    @Override
    public ResponseEntity<CampaignStatisticsResponse> getCampaignStatistics(String xPagopaPnCxId, String campaignId) {
        log.info("[enter] getCampaignStatistics campaignId={}", campaignId);
        return ResponseEntity.ok(campaignStatisticsService.getCampaignStatistics(xPagopaPnCxId, campaignId));
    }

}
