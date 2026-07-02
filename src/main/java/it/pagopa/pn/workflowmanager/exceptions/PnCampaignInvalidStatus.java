package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.CampaignStatus;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_CAMPAIGN_STATUS;

public class PnCampaignInvalidStatus extends PnInternalException {
    public PnCampaignInvalidStatus(String campaignId, CampaignStatus status) {
        super(String.format("Campaign %s has %s status", campaignId, status), ERROR_CODE_WORKFLOWMANAGER_INVALID_CAMPAIGN_STATUS);
    }
}
