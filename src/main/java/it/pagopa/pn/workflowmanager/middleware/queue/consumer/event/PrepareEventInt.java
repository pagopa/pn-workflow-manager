package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ReceiverAddressSourceTypeInt;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@SuperBuilder(toBuilder = true)
public class PrepareEventInt extends PaperEventInt {
    private PhysicalAddressInt receiverAddress;
    private List<String> replacedF24AttachmentUrls;
    private CategorizedAttachmentsResultInt categorizedAttachmentsResult;
    private String productType;
    private String failureDetailCode;
    private ReceiverAddressSourceTypeInt receiverAddressSourceType;

    public enum STATUS_CODE{
        OK,
        PROGRESS,
        KO
    }
}