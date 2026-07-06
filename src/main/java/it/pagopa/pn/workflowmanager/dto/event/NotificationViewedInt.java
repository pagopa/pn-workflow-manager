package it.pagopa.pn.workflowmanager.dto.event;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger.ChannelEventTrigger;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationViewedInt implements ChannelEventTrigger {
    String iun;
    Integer recipientIndex;
    Instant viewedDate;
    String sourceChannel;
    String sourceChannelDetails;
}
