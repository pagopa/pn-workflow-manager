package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;

public interface CampaignService {
    Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId);
}
