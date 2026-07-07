package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.safestorage;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnSafeStorageClientImplTest {
    @Mock
    private FileUploadApi fileUploadApi;
    @Mock
    private FileDownloadApi fileDownloadApi;
    @Mock
    private PnWorkflowManagerConfigs cfg;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private PnSafeStorageClientImpl client;

    @BeforeEach
    void setUp() {
        when(cfg.getCxId()).thenReturn("cx-id");
    }

    @Test
    void shouldInvokeCreateFileUsingConfiguredCxId() {
        FileCreationRequest request = new FileCreationRequest();
        FileCreationResponse expected = new FileCreationResponse();
        String checksumValue = "checksum-value";
        String checksum = "SHA-256";

        when(fileUploadApi.createFile(eq("cx-id"), eq(checksumValue), eq(checksum), eq(request)))
                .thenReturn(expected);

        FileCreationResponse result = client.createFile(checksumValue, checksum, request);

        assertSame(expected, result);
        verify(fileUploadApi).createFile("cx-id", checksumValue, checksum, request);
    }

    @Test
    void shouldInvokeGetFileUsingConfiguredCxId() {
        String fileKey = "safe/file-key";
        Boolean metadataOnly = Boolean.TRUE;
        Boolean tags = Boolean.FALSE;
        FileDownloadResponse expected = new FileDownloadResponse();

        when(fileDownloadApi.getFile(eq(fileKey), eq("cx-id"), eq(metadataOnly), eq(tags)))
                .thenReturn(expected);

        FileDownloadResponse result = client.getFile(fileKey, metadataOnly, tags);

        assertSame(expected, result);
        verify(fileDownloadApi).getFile(fileKey, "cx-id", metadataOnly, tags);
    }
}
