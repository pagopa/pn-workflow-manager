package it.pagopa.pn.workflowmanager.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowManagerExceptionCodesTest {

    @Test
    void testExceptionCodeConstant() {
        assertEquals("PN_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED",
                WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED);
        assertEquals("PN_DELIVERY_CAMPAIGN_NOT_FOUND",
                WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERY_CAMPAIGN_NOT_FOUND);
    }
}

