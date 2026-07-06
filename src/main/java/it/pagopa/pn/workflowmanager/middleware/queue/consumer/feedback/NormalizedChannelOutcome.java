package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback;

import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class NormalizedChannelOutcome {
    private String iun;
    private int recIndex;
    private ChannelType channel;
    private FeedbackClassification classification;
    private Set<ChannelEventTrigger> triggers;
    private String originalEventType;
    private Instant eventTimestamp;
    private TimelineElementInternal timelineElementInternal;
}
