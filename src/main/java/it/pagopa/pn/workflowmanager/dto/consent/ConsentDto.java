package it.pagopa.pn.workflowmanager.dto.consent;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder( toBuilder = true )
public class ConsentDto {
    private String type;
    private int version;
}
