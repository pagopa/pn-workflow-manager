package it.pagopa.pn.workflowmanager.dto.event;


import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.trigger.ChannelEventTrigger;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationPaidInt implements ChannelEventTrigger {
    private final String iun;
    private final int recIndex;
    private final String paymentSourceChannel;
    private final Instant eventTimestamp;
    private final String creditorTaxId;
    private final String noticeCode;
    private final int amount;
}
