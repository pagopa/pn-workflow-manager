package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.safestorage;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.dto.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.UpdateFileMetadataRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@CustomLog
@RequiredArgsConstructor
@Component
public class PnSafeStorageClientImpl implements PnSafeStorageClient {
    private final FileUploadApi fileUploadApi;
    private final FileDownloadApi fileDownloadApi;
    private final PnWorkflowManagerConfigs cfg;
    private final RestTemplate restTemplate;

    @Override
    public FileCreationResponse createFile(String checksumValue, String checksum, FileCreationRequest fileCreationRequest) {
        log.logInvokingExternalService(CLIENT_NAME, CREATE_FILE);
        return fileUploadApi.createFile(cfg.getCxId(), checksumValue, checksum, fileCreationRequest);
    }

    @Override
    public FileDownloadResponse getFile(String fileKey, Boolean metadataOnly, Boolean tags) {
        log.logInvokingExternalService(CLIENT_NAME, GET_FILE);
        return fileDownloadApi.getFile(fileKey, cfg.getCxId(), metadataOnly, tags);
    }

    @Override
    public UpdateFileMetadataResponseInt updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest) {
        log.logInvokingExternalService(CLIENT_NAME, "UPDATE FILE METADATA");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-pagopa-safestorage-cx-id", cfg.getCxId());
        HttpEntity<UpdateFileMetadataRequest> requestEntity = new HttpEntity<>(updateFileMetadataRequest, headers);
        String url = cfg.getSafeStorageBaseUrl() + "/safe-storage/v1/files/{fileKey}";

        try {
            ResponseEntity<UpdateFileMetadataResponseInt> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UpdateFileMetadataResponseInt.class,
                    fileKey
            );
            if (response.getBody() == null) {
                throw new PnInternalException("Empty response while updating metadata", WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UPLOADFILEERROR);
            }
            return response.getBody();
        } catch (RestClientException exception) {
            throw new PnInternalException("Cannot update file metadata", WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UPLOADFILEERROR, exception);
        }
    }

    @Override
    public void uploadContent(FileCreationWithContentRequest fileCreationRequest, FileCreationResponse fileCreationResponse, String sha256) {
        try {
            log.logInvokingAsyncExternalService(CLIENT_NAME, UPLOAD_FILE_CONTENT, fileCreationResponse.getKey());

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Content-type", fileCreationRequest.getContentType());
            headers.add("x-amz-checksum-sha256", sha256);
            headers.add("x-amz-meta-secret", fileCreationResponse.getSecret());

            HttpEntity<Resource> req = new HttpEntity<>(new ByteArrayResource(fileCreationRequest.getContent()), headers);

            URI url = URI.create(fileCreationResponse.getUploadUrl());
            HttpMethod method = fileCreationResponse.getUploadMethod() == FileCreationResponse.UploadMethodEnum.POST ? HttpMethod.POST : HttpMethod.PUT;

            ResponseEntity<String> res = restTemplate.exchange(url, method, req, String.class);

            if (res.getStatusCode().value() != HttpStatus.OK.value())
            {
                throw new PnInternalException("File upload failed", WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UPLOADFILEERROR);
            }

        } catch (PnInternalException ee)
        {
            log.error("uploadContent PnInternalException uploading file", ee);
            throw ee;
        }
        catch (Exception ee)
        {
            log.error("uploadContent Exception uploading file", ee);
            throw new PnInternalException("Exception uploading file", WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UPLOADFILEERROR, ee);
        }
    }
}
