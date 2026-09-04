package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.io.IoSendMessageResultInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendCourtesyMessageDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, CourtesyAddressRelatedTimelineElement {
    private int recIndex;
    private CourtesyDigitalAddressInt digitalAddress;
    private Instant sendDate;
    private IoSendMessageResultInt ioSendMessageResult;
    
    public String toLog() {
        return String.format(
                "recIndex=%d addressType=%s digitalAddress=%s",
                recIndex,
                digitalAddress != null ? digitalAddress.getType() : null,
                AuditLogUtils.SENSITIVE
        );
    }
}
