package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;


import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.CategoryTypeTimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.RecipientRelatedTimelineElementDetails;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendDigitalMessageDetailsInt extends CategoryTypeTimelineElementDetailsInt implements InformalDigitalAddressRelatedTimelineElement, RecipientRelatedTimelineElementDetails {
    private int recIndex;
    private InformalDigitalAddressInt digitalAddress;
    private DigitalAddressSourceInt digitalAddressSource;
    private DigitalChannelsInt channel;
    private Integer retryNumber;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d channel=%s digitalAddressSource=%s retryNumber=%s digitalAddress=%s",
                recIndex,
                channel,
                digitalAddressSource,
                retryNumber,
                AuditLogUtils.SENSITIVE
        );
    }
}
