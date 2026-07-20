package it.pagopa.pn.workflowmanager.exceptions;

import lombok.Getter;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERY_CAMPAIGN_NOT_FOUND;

@Getter
public class PnCampaignNotFoundException extends PnNotFoundException {

    public PnCampaignNotFoundException(String description) {
        super("Campaign not found", description, ERROR_CODE_DELIVERY_CAMPAIGN_NOT_FOUND);
    }
}

