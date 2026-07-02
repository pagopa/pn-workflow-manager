package it.pagopa.pn.workflowmanager.dto.action.details;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreationResponseActionDetails implements ActionDetails {
    private String key;
    private String documentCreationType;
}
