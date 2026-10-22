package it.pagopa.pn.workflowmanager.dto.timeline.details;

import it.pagopa.pn.workflowmanager.utils.AuditLogUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PublicRegistryValidationResponseDetailsInt extends PublicRegistryResponseDetailsInt implements PhysicalAddressRelatedTimelineElement {
    private String registry;

    @Override
    public String toLog() {
        return String.format(
                "recIndex=%d digitalAddress=%s physicalAddress=%s requestTimelineId=%s registry=%s",
                recIndex,
                AuditLogUtils.SENSITIVE,
                AuditLogUtils.SENSITIVE,
                requestTimelineId,
                registry
        );
    }

}
