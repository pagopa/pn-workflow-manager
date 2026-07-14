package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.DocumentType;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationResponseInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.service.SafeStorageService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveDocumentServiceImplTest {

    @Mock
    private TemplateGeneratorService templateGeneratorService;

    @Mock
    private SafeStorageService safeStorageService;

    private SaveDocumentServiceImpl saveDocumentService;

    @BeforeEach
    void setUp() {
        saveDocumentService = new SaveDocumentServiceImpl(templateGeneratorService, safeStorageService);
    }

    @Test
    void saveDocumentShouldUploadPdfWithMetadataAndReturnPrefixedKey() {
        byte[] content = "legal-fact".getBytes(StandardCharsets.UTF_8);
        Map<String, List<String>> tags = Map.of("iun", List.of("IUN123"));

        when(safeStorageService.createAndUploadContent(any()))
                .thenReturn(new FileCreationResponseInt("generatedKey"));

        String result = saveDocumentService.saveDocument(content, tags);

        assertEquals("safestorage://generatedKey", result);

        ArgumentCaptor<FileCreationWithContentRequest> requestCaptor = ArgumentCaptor.forClass(FileCreationWithContentRequest.class);
        verify(safeStorageService).createAndUploadContent(requestCaptor.capture());
        FileCreationWithContentRequest req = requestCaptor.getValue();
        assertEquals(SaveDocumentServiceImpl.DOCUMENTS_MEDIATYPE_STRING, req.getContentType());
        assertEquals(SaveDocumentServiceImpl.PN_COMMUNICATIONS_COVERPAGE, req.getDocumentType());
        assertEquals(SaveDocumentServiceImpl.SAVED, req.getStatus());
        assertArrayEquals(content, req.getContent());
        assertEquals(tags, req.getTags());
    }

    @Test
    void saveDocumentShouldReturnUnchangedKeyWhenStoragePrefixIsAlreadyPresent() {
        when(safeStorageService.createAndUploadContent(any()))
                .thenReturn(new FileCreationResponseInt("safestorage://generatedKey"));

        String result = saveDocumentService.saveDocument(new byte[0], Map.of());

        assertEquals("safestorage://generatedKey", result);
    }

    @Test
    void saveCoverpageShouldGenerateAndStoreCoverpageWithExpectedTags() {
        byte[] fileBytes = "pdfcontent".getBytes(StandardCharsets.UTF_8);

        NotificationInt notification = NotificationInt.builder().iun("iun-123").build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().taxId("tax-1").build();
        Campaign campaign = Campaign.builder().campaignId("camp-1").senderId("sender-1").build();

        when(templateGeneratorService.generateCoverpageTemplate(notification, recipient, campaign)).thenReturn(fileBytes);
        when(safeStorageService.createAndUploadContent(any())).thenReturn(new FileCreationResponseInt("k1"));

        String res = saveDocumentService.saveCoverpage(notification, recipient, campaign,"timeline-1", 0);

        assertEquals("safestorage://k1", res);

        ArgumentCaptor<FileCreationWithContentRequest> requestCaptor = ArgumentCaptor.forClass(FileCreationWithContentRequest.class);
        verify(safeStorageService).createAndUploadContent(requestCaptor.capture());
        FileCreationWithContentRequest req = requestCaptor.getValue();
        assertEquals(SaveDocumentServiceImpl.DOCUMENTS_MEDIATYPE_STRING, req.getContentType());
        assertEquals(SaveDocumentServiceImpl.PN_COMMUNICATIONS_COVERPAGE, req.getDocumentType());
        assertEquals(SaveDocumentServiceImpl.SAVED, req.getStatus());
        assertArrayEquals(fileBytes, req.getContent());

        assertEquals(Map.of(
                "iun", List.of("iun-123"),
                "recIndex", List.of("0"),
                "documentType", List.of(DocumentType.COVERPAGE.name()),
                "timelineElementId", List.of("timeline-1")
        ), req.getTags());
    }

    @Test
    void saveCoverpageShouldWrapStorageFailuresInPnInternalException() {
        byte[] fileBytes = "pdfcontent".getBytes(StandardCharsets.UTF_8);
        NotificationInt notification = NotificationInt.builder().iun("iun-999").build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().taxId("tax-999").build();
        Campaign campaign = Campaign.builder().campaignId("camp-1").senderId("sender-1").build();
        RuntimeException storageException = new RuntimeException("generation failed");

        when(templateGeneratorService.generateCoverpageTemplate(notification, recipient, campaign)).thenReturn(fileBytes);
        when(safeStorageService.createAndUploadContent(any())).thenThrow(storageException);

        PnInternalException exc = assertThrows(PnInternalException.class, () ->
                saveDocumentService.saveCoverpage(notification, recipient, campaign, "t-1", 1)
        );

        assertEquals(String.format(SaveDocumentServiceImpl.SAVE_DOCUMENT_EXCEPTION_MESSAGE,
                "COVERPAGE", notification.getIun(), 1), exc.getProblem().getDetail());
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc.getProblem().getErrors().getFirst().getCode());
        assertSame(storageException, exc.getCause());
    }
}
