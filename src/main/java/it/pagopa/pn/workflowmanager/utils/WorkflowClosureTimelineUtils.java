package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InvoiceableTimelineElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowClosureTimelineUtils {
    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;

    /**
     * Se esiste già un elemento fatturabile, il costo è già stato addebitato.
     * Altrimenti si usa il costo base configurato.
     */
    public Integer getNotificationCostToApply(List<TimelineElementInternal> timelineElements) {
        if (containsInvoiceableElement(timelineElements)) {
            log.info("Invoiceable timeline element already present, notification cost set to null");
            return null;
        }
        Integer notificationCost = pnWorkflowManagerConfigs.getPagoPaInformalNotificationBaseCost();
        log.info("No invoiceable timeline element found, applying base notification cost {}", notificationCost);
        return notificationCost;
    }

    private static boolean containsInvoiceableElement(List<TimelineElementInternal> timelineElements) {
        return Objects.nonNull(timelineElements) && timelineElements.stream()
                .anyMatch(element -> element.getDetails() instanceof InvoiceableTimelineElement);
    }
}
