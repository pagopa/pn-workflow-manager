package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelOutcomeEvent;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class ExtChannelOutcomeEvent implements ChannelOutcomeEvent {
    private String requestId;
    private Instant eventTimestamp;
    private ExtChannelOutcomeStatusInt status;
    private String eventDetails;
    private DigitalMessageReferenceInt generatedMessage;
    private ExtChannelOutcomeEventCodeInt eventCode;

    @Override
    public String getRequestId() {
        return requestId;
    }
}

