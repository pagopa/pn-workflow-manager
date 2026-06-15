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
        String msg = "getCampaignStatistics campaignId={} ";
        String[] args = new String[]{campaignId};
        generateAuditLog(PnAuditLogEventType.AUD_WH_READ, msg, args).log();

        return campaignStatisticsEntityDao.get(campaignId)
                .switchIfEmpty(Mono.error(new PnCampaignStatisticsNotFoundException("Campaign with  id: " + campaignId + " not found ")))
                .map(EntityToDtoCampaignStatisticsMapper::entityToDto)
                .doOnSuccess(entity ->
                        generateAuditLog(PnAuditLogEventType.AUD_WH_READ, msg, args).generateSuccess().log()
                )
                .doOnError(err -> generateAuditLog(PnAuditLogEventType.AUD_WH_READ, msg, args).generateFailure("error getting campaign statistics", err).log());
    }

    @NotNull
    protected PnAuditLogEvent generateAuditLog(PnAuditLogEventType pnAuditLogEventType, String message, String[] arguments) {
        String logMessage = MessageFormatter.arrayFormat(message, arguments).getMessage();
        PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
        PnAuditLogEvent logEvent;
        logEvent = auditLogBuilder.before(pnAuditLogEventType, "{}", logMessage)
                .build();
        return logEvent;
    }

}
