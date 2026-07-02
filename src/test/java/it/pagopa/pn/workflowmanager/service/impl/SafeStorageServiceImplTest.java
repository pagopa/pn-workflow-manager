package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class SafeStorageServiceImplTest {
    @Mock
    private PnSafeStorageClient safeStorageClient;

    private SafeStorageServiceImpl safeStorageService;

    @BeforeEach
    public void init(){
        safeStorageService = new SafeStorageServiceImpl( safeStorageClient);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void createAndUploadContent() {
        //GIVEN
        FileCreationWithContentRequest fileCreationWithContentRequest = new FileCreationWithContentRequest();
        fileCreationWithContentRequest.setContent("content".getBytes());

        FileCreationResponse expectedResponse = new FileCreationResponse();
        expectedResponse.setKey("key");
        expectedResponse.setSecret("secret");

        Mockito.when(safeStorageClient.createFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(FileCreationWithContentRequest.class)))
                .thenReturn(expectedResponse);

        //WHEN
        FileCreationResponseInt response = safeStorageService.createAndUploadContent(fileCreationWithContentRequest);

        //THEN
        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getKey(), expectedResponse.getKey());
    }
}