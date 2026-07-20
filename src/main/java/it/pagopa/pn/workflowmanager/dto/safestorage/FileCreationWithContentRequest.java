package it.pagopa.pn.workflowmanager.dto.safestorage;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileCreationWithContentRequest extends FileCreationRequest {
    private byte[] content;
}
