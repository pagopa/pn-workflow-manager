package it.pagopa.pn.workflowmanager.dto.ext.publicregistry;

import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class NationalRegistriesResponse {
    private String correlationId;
    private Integer recIndex;
    private String registry;
    private String error;
    private Integer errorStatus;
    private LegalDigitalAddressInt digitalAddress;
    private PhysicalAddressInt physicalAddress;
}
