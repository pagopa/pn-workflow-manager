package it.pagopa.pn.workflowmanager.dto.consent;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@AllArgsConstructor
@ToString
@SuperBuilder( toBuilder = true )
public class ConsentDto {
    private final String type;
    private final String version;
}
