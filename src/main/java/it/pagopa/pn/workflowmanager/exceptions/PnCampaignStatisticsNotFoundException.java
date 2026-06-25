package it.pagopa.pn.workflowmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;

public class PnCampaignStatisticsNotFoundException extends PnRuntimeException {

    public PnCampaignStatisticsNotFoundException(String message) {
        super(message, WorkflowManagerExceptionCodes.ERROR_CODE_CAMPAIGN_STATISTICS_NOT_FOUND, 404, WorkflowManagerExceptionCodes.ERROR_CODE_CAMPAIGN_STATISTICS_NOT_FOUND, null, message);

    }
}
