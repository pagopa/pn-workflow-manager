package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.CoverpageCreationRequestDetailsInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimelineUtils {

    private final TimelineService timelineService;

    public String retrieveCoverpageFileKey(String iun, int recIndex) {
        String timelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        log.debug("retrieveCoverpageFileKey - iun={} recIndex={} timelineId={}", iun, recIndex, timelineId);

        TimelineElementInternal timelineElement = timelineService.getTimelineElementStrongly(iun, timelineId)
                .orElseThrow(() -> buildTimelineElementNotPresentException(iun, recIndex, timelineId));

        if (!(timelineElement.getDetails() instanceof CoverpageCreationRequestDetailsInt details)
                || details.getFileKey() == null
                || details.getFileKey().isBlank()) {
            String msg = String.format(
                    "Timeline element %s for iun=%s recIndex=%d does not contain a valid coverpage fileKey",
                    timelineId,
                    iun,
                    recIndex
            );
            log.error(msg);
            throw new PnInternalException(msg, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }

        return details.getFileKey();
    }

    private PnInternalException buildTimelineElementNotPresentException(String iun, int recIndex, String timelineId) {
        String msg = String.format(
                "Timeline element %s not found for iun=%s recIndex=%d while retrieving coverpage fileKey",
                timelineId,
                iun,
                recIndex
        );
        log.error(msg);
        return new PnInternalException(msg, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
    }
}
