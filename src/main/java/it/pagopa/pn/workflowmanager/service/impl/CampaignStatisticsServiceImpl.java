package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignNotFoundException;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignStatisticsNotFoundException;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.CampaignStatisticsEntityDao;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.entity.CampaignStatisticsEntity;
import it.pagopa.pn.workflowmanager.middleware.dao.dynamo.mapper.EntityToDtoCampaignStatisticsMapper;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CampaignStatisticsServiceImpl implements CampaignStatisticsService {

    private final CampaignStatisticsEntityDao campaignStatisticsEntityDao;
    private final CampaignServiceCachedProvider campaignServiceCachedProvider;

    @Override
    public CampaignStatisticsResponse getCampaignStatistics(
            String xPagopaPnCxId, String campaignId) {

        log.debug("getCampaignStatistics for campaignId={}", campaignId);

        CampaignStatisticsEntity entity = campaignStatisticsEntityDao
                .get(xPagopaPnCxId, campaignId)
                .orElseThrow(() -> {
                    var campaign = campaignServiceCachedProvider
                            .getByCampaignIdAndSenderId(campaignId, xPagopaPnCxId);

                    if (campaign == null) {
                        log.warn("Campaign not found for campaignId={} senderId={}",
                                campaignId, xPagopaPnCxId);
                        return new PnCampaignNotFoundException(
                                "Campaign with id: " + campaignId + " Not Found");
                    }

                    log.warn("Statistics not found for campaignId={} senderId={}",
                            campaignId, xPagopaPnCxId);
                    return new PnCampaignStatisticsNotFoundException(
                            "Statistics not found for campaign with id: " + campaignId);
                });

        return EntityToDtoCampaignStatisticsMapper.entityToDto(entity);
    }
}
