package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.safestorage;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.dto.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.UpdateFileMetadataRequest;

public interface PnSafeStorageClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_SAFE_STORAGE;

    String CREATE_FILE = "CREATE FILE";
    String GET_FILE = "GET FILE";
    String SAFE_STORAGE_URL_PREFIX = "safestorage://";
    String UPLOAD_FILE_CONTENT = "UPLOAD FILE CONTENT";

    FileCreationResponse createFile(String checksumValue, String checksum, FileCreationRequest fileCreationRequest);

    FileDownloadResponse getFile(String fileKey, Boolean metadataOnly, Boolean tags);

    UpdateFileMetadataResponseInt updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest);

    void uploadContent(FileCreationWithContentRequest fileCreationRequest, FileCreationResponse fileCreationResponse, String sha256);
}
