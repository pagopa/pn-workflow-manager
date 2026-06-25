package it.pagopa.pn.workflowmanager.dto.timeline.details.informal;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.CategoryTypeTimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.DigitalAddressSourceRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.RecipientRelatedTimelineElementDetails;
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
public class SendDigitalMessageFeedbackDetailsInt extends CategoryTypeTimelineElementDetailsInt implements RecipientRelatedTimelineElementDetails,
        InformalDigitalAddressRelatedTimelineElement, DigitalAddressSourceRelatedTimelineElement {
    private int recIndex;
    private InformalDigitalAddressInt digitalAddress;
    private DigitalAddressSourceInt digitalAddressSource;
    private ResponseStatusInt responseStatus;
    private Instant notificationDate;
    private DigitalChannelsInt channel;
    private DigitalDeliveryDetailsInt deliveryDetail;
    private List<SendingReceipt> sendingReceipts;
    private String requestId;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d responseStatus=%s requestId=%s channel=%s digitalAddressSource=%s deliveryDetail=%s digitalAddress=%s",
                recIndex,
                responseStatus,
                requestId,
                channel,
                digitalAddressSource,
                deliveryDetail,
                AuditLogUtils.SENSITIVE
        );
    }
}
