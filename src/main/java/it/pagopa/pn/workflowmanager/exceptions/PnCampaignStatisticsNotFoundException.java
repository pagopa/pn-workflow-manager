package it.pagopa.pn.workflowmanager.exceptions;

import lombok.Getter;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND;

@Getter
public class PnCampaignStatisticsNotFoundException extends PnNotFoundException {

    public PnCampaignStatisticsNotFoundException(String description) {
        super("Campaign statistics not found", description,
                ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND);
    }
}