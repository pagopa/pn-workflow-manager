package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class PaymentNoticePaidDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private String recipientType;
    private String iuv;
    private String noticeCode;
    private String creditorTaxId;
    private String companyName;
    private Instant eventTimestamp;
    private String dueDate;
    private String paymentSourceChannel;
    private Integer amount;
    private Integer fee;
    private String eventId;
    private Instant pagopaPersistedTimestamp;
    private Instant pagopaPublishedTimestamp;
    private Instant sendIngestionTimestamp;

    public Instant getElementTimestamp() {
        return eventTimestamp;
    }

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d recipientType=%s iuv=%s noticeCode=%s creditorTaxId=%s companyName=%s eventTimestamp=%s dueDate=%s paymentSourceChannel=%s amount=%s fee=%s eventId=%s pagopaPersistedTimestamp=%s pagopaPublishedTimestamp=%s sendIngestionTimestamp=%s",
                recIndex,
                recipientType,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                eventTimestamp,
                dueDate,
                paymentSourceChannel,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                pagopaPersistedTimestamp,
                pagopaPublishedTimestamp,
                sendIngestionTimestamp
        );
    }
}
