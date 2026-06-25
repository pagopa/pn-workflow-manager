package it.pagopa.pn.workflowmanager.dto.ext.externalchannel;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class ResultFilterInt {

    private String fileKey;
    private ResultFilterEnum result;
    private String reasonCode;
    private String reasonDescription;
}
