package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
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
public class PrepareAnalogDeliveryDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, PhysicalAddressRelatedTimelineElement {
    private int recIndex;
    private PhysicalAddressInt physicalAddress;
    private ServiceLevelInt serviceLevel;
    private AnalogDeliveryTypeInt deliveryType;
    private Integer sentAttemptMade;
    private String relatedRequestId;
    private String foreignState;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d serviceLevel=%s deliveryType=%s sentAttemptMade=%s relatedRequestId=%s foreignState=%s physicalAddress=%s",
                recIndex,
                serviceLevel,
                deliveryType,
                sentAttemptMade,
                relatedRequestId,
                foreignState,
                AuditLogUtils.SENSITIVE
        );
    }
}
