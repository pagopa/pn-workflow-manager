package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignStatisticsNotFoundException;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.mapper.EntityToDtoCampaignStatisticsMapper;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class CampaignStatisticsServiceImpl implements CampaignStatisticsService {

    private final CampaignStatisticsEntityDao campaignStatisticsEntityDao;

    @Override
    public Mono<CampaignStatisticsResponse> getCampaignStatistics(String campaignId) {
        log.debug(MessageFormatter.arrayFormat("getCampaignStatistics campaignId={}", new Object[]{campaignId}).getMessage());

        return campaignStatisticsEntityDao.get(campaignId)
                .switchIfEmpty(Mono.error(new PnCampaignStatisticsNotFoundException("Campaign with id: " + campaignId + " not found ")))
                .map(EntityToDtoCampaignStatisticsMapper::entityToDto)
                .doOnSuccess(entity ->
                        log.info(MessageFormatter.arrayFormat("getCampaignStatistics campaignId={} result={}", new Object[]{campaignId, entity}).getMessage())
                )
                .doOnError(err -> log.error(MessageFormatter.arrayFormat("getCampaignStatistics campaignId={} error={}", new Object[]{campaignId, err.getMessage()}).getMessage()));
    }

}
