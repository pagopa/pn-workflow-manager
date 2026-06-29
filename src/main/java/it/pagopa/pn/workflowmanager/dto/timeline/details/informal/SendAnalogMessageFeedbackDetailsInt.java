package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;


import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.AnalogDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.*;
import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString
public class SendAnalogMessageFeedbackDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails,
        NewAddressRelatedTimelineElement, PhysicalAddressRelatedTimelineElement {
    private int recIndex;
    private PhysicalAddressInt physicalAddress;
    private ServiceLevelInt serviceLevel;
    private Integer sentAttemptMade;
    private PhysicalAddressInt newAddress;
    private AnalogDeliveryDetailsInt deliveryDetail;
    private AnalogDeliveryTypeInt deliveryType;
    private ResponseStatusInt responseStatus;
    private List<SendingReceipt> sendingReceipts;
    private String requestTimelineId;
    private Instant notificationDate;
    private List<AttachmentDetailsInt> attachments;
    private String sendRequestId;
    private String registeredLetterCode;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d serviceLevel=%s sentAttemptMade=%s deliveryType=%s responseStatus=%s requestTimelineId=%s notificationDate=%s attachments=%s sendRequestId=%s registeredLetterCode=%s deliveryDetail=%s physicalAddress=%s newAddress=%s",
                recIndex,
                serviceLevel,
                sentAttemptMade,
                deliveryType,
                responseStatus,
                requestTimelineId,
                notificationDate,
                attachments,
                sendRequestId,
                registeredLetterCode,
                deliveryDetail,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE
        );
    }
}
