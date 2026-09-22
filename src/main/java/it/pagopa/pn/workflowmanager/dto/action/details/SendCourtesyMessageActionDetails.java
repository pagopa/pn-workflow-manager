package it.pagopa.pn.workflowmanager.dto.action.details;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendCourtesyMessageActionDetails implements ActionDetails {
    private CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel;
    private int retryIndex;
    private DeliveryModeInt deliveryMode;
    private List<CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT> plannedChannels;
}
