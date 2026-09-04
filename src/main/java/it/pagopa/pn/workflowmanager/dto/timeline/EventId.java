package it.pagopa.pn.workflowmanager.dto.timeline;

import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class EventId {
    private String iun;
    private Integer recIndex;
    private DigitalAddressSourceInt source;
    private Integer sentAttemptMade;
    private Integer progressIndex;
    private String deliveryType;
    private String channel;
    private CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT courtesyAddressType;
    private String creditorTaxId;
    private String noticeCode;
    private ContactPhaseInt contactPhase;
    private DeliveryModeInt deliveryMode;
}
