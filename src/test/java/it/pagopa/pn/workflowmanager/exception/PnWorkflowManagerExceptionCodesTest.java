package it.pagopa.pn.workflowmanager.exception;

import it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PnWorkflowManagerExceptionCodesTest {

    @Test
    void checkAll() {
        Assertions.assertAll(
                () -> Assertions.assertEquals("PN_TIMELINESERVICE_TIMELINEELEMENTNOTPRESENT", WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT),
                () -> Assertions.assertEquals("PN_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED", WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_ACTIONTYPENOTSUPPORTED));
    }

}