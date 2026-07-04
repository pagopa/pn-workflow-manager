package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class IoOutcomeEvent {
    private String xPagopaIoConCxId;
    private String requestId;
    private String ioMessageId;
    private String noticeCode;
    private IoOutcomeEventType eventType;
    private Instant eventTimestamp;
}
