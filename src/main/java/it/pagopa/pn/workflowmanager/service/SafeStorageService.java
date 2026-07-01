package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.dto.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.UpdateFileMetadataRequest;

public interface SafeStorageService {
    
    FileCreationResponseInt createAndUploadContent(FileCreationWithContentRequest fileCreationRequest);

    UpdateFileMetadataResponseInt updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest);

    FileDownloadResponse getFile(String fileKey, Boolean metadataOnly, Boolean tags);
}
