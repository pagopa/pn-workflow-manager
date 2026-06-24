package it.pagopa.pn.workflowmanager.dto.timeline.details.common;

import java.time.Instant;

public interface ElementTimestampTimelineElementDetails extends TimelineElementDetailsInt {

    Instant getElementTimestamp();
}
