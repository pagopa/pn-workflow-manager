package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
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
    private ChannelOutcomeClassification classification;
    private Set<ChannelEventTrigger> triggers;
    private String originalEventType;
    private Instant eventTimestamp;
    private TimelineElementInternal timelineElementInternal;
    private PnAuditLogEvent pnAuditLogEvent;
}
