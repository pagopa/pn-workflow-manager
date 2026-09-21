package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.CourtesyAddressRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.CourtesyMessageProgressEvent;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;

@Component
@RequiredArgsConstructor
public class CourtesyEventOriginResolver {
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;

    public CourtesyEventOrigin resolveOrigin(CourtesyMessageProgressEvent event) {
        String iun = timelineUtils.getIunFromTimelineId(event.getRequestId());
        Optional<TimelineElementInternal> optTimelineElement = timelineService.getTimelineElement(iun, event.getRequestId());
        if(optTimelineElement.isEmpty()) {
            throw new PnInternalException(
                    "Timeline element not found for requestId: " + event.getRequestId(),
                    ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED
            );
        }
        TimelineElementDetailsInt details = optTimelineElement.get().getDetails();
        if(details instanceof CourtesyAddressRelatedTimelineElement) {
            return CourtesyEventOrigin.COURTESY_MESSAGE;
        } else if(details instanceof SendRelatedTimelineElement) {
            return CourtesyEventOrigin.CHANNEL_MESSAGE;
        } else {
            throw new PnInternalException(
                    "Timeline element details are not of expected type for requestId: " + event.getRequestId(),
                    ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED
            );
        }
    }

    public enum CourtesyEventOrigin {
        CHANNEL_MESSAGE,
        COURTESY_MESSAGE
    }
}
