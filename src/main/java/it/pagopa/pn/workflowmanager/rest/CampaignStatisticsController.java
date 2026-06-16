package it.pagopa.pn.workflowmanager.rest;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.api.CampaignStatisticsApiDelegate;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import it.pagopa.pn.workflowmanager.service.utils.MdcKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CampaignStatisticsController implements CampaignStatisticsApiDelegate {

    private final CampaignStatisticsService campaignStatisticsService;

    @Override
    public Mono<ResponseEntity<CampaignStatisticsResponse>> getCampaignStatistics(String campaignId, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_CTX_TOPIC, MdcKey.CAMPAIGN_STATISTICS_KEY);
        log.info("[enter] getCampaignStatistics streamId={}", campaignId);

        return MDCUtils.addMDCToContextAndExecute(
            campaignStatisticsService.getCampaignStatistics(campaignId)
                        .map(ResponseEntity::ok)
        );
    }

}
