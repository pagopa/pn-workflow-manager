package it.pagopa.pn.workflowmanager.dto.timeline.details;


import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendAnalogMessageDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails, PhysicalAddressRelatedTimelineElement {
    private int recIndex;
    private PhysicalAddressInt physicalAddress;
    private ServiceLevelInt serviceLevel;
    private AnalogDeliveryTypeInt deliveryType;
    private Integer sentAttemptMade;
    private String relatedRequestId;
    private String foreignState;
    private String productType;
    private Integer analogCost;
    private Integer numberOfPages;
    private Integer envelopeWeight;
    private String prepareRequestId;
    private List<String> f24Attachments;
    private CategorizedAttachmentsResultInt categorizedAttachmentsResult;
    private Integer vat;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d serviceLevel=%s deliveryType=%s sentAttemptMade=%s relatedRequestId=%s foreignState=%s productType=%s analogCost=%s numberOfPages=%s envelopeWeight=%s prepareRequestId=%s f24Attachments=%s vat=%s physicalAddress=%s",
                recIndex,
                serviceLevel,
                deliveryType,
                sentAttemptMade,
                relatedRequestId,
                foreignState,
                productType,
                analogCost,
                numberOfPages,
                envelopeWeight,
                prepareRequestId,
                f24Attachments,
                vat,
                AuditLogUtils.SENSITIVE
        );
    }
}
