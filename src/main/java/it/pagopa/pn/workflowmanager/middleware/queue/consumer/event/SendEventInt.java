package it.pagopa.pn.workflowmanager.middleware.queue.consumer.event;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendEventInt extends PaperEventInt {
    private String statusDescription;
    private List<AttachmentDetailsInt> attachments = null;
    private PhysicalAddressInt discoveredAddress;
    private String deliveryFailureCause;
    private String registeredLetterCode;
    private String sendRequestId;

    @Override
    public String getRequestId() {
        return sendRequestId;
    }
}
