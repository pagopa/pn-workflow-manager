package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.WorkflowDoneReachedDetailsInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationCostUtilsTest {

    @Test
    void getNotificationCostToApplyOnlyChargesWhenRecipientHasNoInvoiceableElement() {
        PnWorkflowManagerConfigs configs = new PnWorkflowManagerConfigs();
        configs.setPagoPaInformalNotificationBaseCost(250);
        NotificationCostUtils utils = new NotificationCostUtils(configs);

        assertEquals(250, utils.getNotificationCostToApply(
                List.of(TimelineElementInternal.builder().build()), 0));

        List<TimelineElementInternal> timeline = List.of(TimelineElementInternal.builder()
                .details(WorkflowDoneReachedDetailsInt.builder()
                        .recIndex(0)
                        .notificationCost(250)
                        .build())
                .build());

        assertNull(utils.getNotificationCostToApply(timeline, 0));
        assertEquals(250, utils.getNotificationCostToApply(timeline, 1));
    }
}
