package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.config.CampaignsParameterConsumer;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignInvalidStatus;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.CampaignStatus;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CampaignServiceImpl implements CampaignService {
    private final CampaignsParameterConsumer campaignsParameterConsumer;

    @Override
    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId) {
        Campaign campaign = campaignsParameterConsumer.getCampaignByCampaignIdAndSenderId(campaignId, senderId);
        if (!CampaignStatus.IN_PROGRESS.equals(campaign.getStatus())) {
            throw new PnCampaignInvalidStatus(campaignId, campaign.getStatus());
        }
        return campaign;
    }
}
