package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;

public interface CampaignService {
    Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId);
}
