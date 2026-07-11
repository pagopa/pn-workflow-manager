package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeEvent;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class PaperEventInt implements ChannelOutcomeEvent {
    private String requestId;
    private String iun;
    private String statusCode;
    private Instant statusDateTime;
    private String statusDetail;

    @Override
    public String getRequestId() {
        return requestId;
    }
}
