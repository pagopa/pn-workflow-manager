package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.dto.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.UpdateFileMetadataRequest;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.workflowmanager.service.SafeStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED;

@Slf4j
@Service
@AllArgsConstructor
public class SafeStorageServiceImpl implements SafeStorageService {
    private final PnSafeStorageClient safeStorageClient;
    
    @Override
    public FileCreationResponseInt createAndUploadContent(FileCreationWithContentRequest fileCreationRequest) {
        log.info("Start createAndUploadFile - documentType={} filesize={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length);

        String sha256 = computeSha256(fileCreationRequest.getContent());
        try {
            var fileCreationResponse = safeStorageClient.createFile(sha256, "SHA256", fileCreationRequest);
            safeStorageClient.uploadContent(fileCreationRequest, fileCreationResponse, sha256);

            FileCreationResponseInt fileCreationResponseInt = FileCreationResponseInt.builder()
                    .key(fileCreationResponse.getKey())
                    .build();
            log.info("createAndUploadContent file uploaded successfully key={} sha256={}", fileCreationResponseInt.getKey(), sha256);
            return fileCreationResponseInt;
        } catch (Exception exception) {
            log.error("Cannot create or upload file", exception);
            throw new PnInternalException("Cannot create file", ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exception);
        }
    }
    
    @Override
    public UpdateFileMetadataResponseInt updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest) {
        MDC.put(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY, fileKey);
        log.debug("Start call updateFileMetadata - fileKey={} updateFileMetadataRequest={}", fileKey, updateFileMetadataRequest);
        try {
            UpdateFileMetadataResponseInt res = safeStorageClient.updateFileMetadata(fileKey, updateFileMetadataRequest);
            log.debug("updateFileMetadata file ok key={} updateFileMetadataResponseInt={}", fileKey, res);
            return res;
        } catch (Exception err) {
            log.error("Cannot update metadata ", err);
            throw new PnInternalException("Cannot update metadata", ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, err);
        } finally {
            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
        }
    }

    @Override
    public FileDownloadResponse getFile(String fileKey, Boolean metadataOnly, Boolean tags) {
        return safeStorageClient.getFile(fileKey, metadataOnly, tags);
    }

    private String computeSha256( byte[] content ) {

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest( content );
            return bytesToBase64( encodedhash );
        } catch (Exception exc) {
            throw new PnInternalException("cannot compute sha256", ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc);
        }
    }

    private static String bytesToBase64(byte[] hash) {
        return Base64.getEncoder().encodeToString( hash );
    }
}
