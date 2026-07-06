package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @TempDir
    Path tempDir;

    private SaveDocumentServiceImpl saveDocumentService;

    @BeforeEach
    void setUp() {
        saveDocumentService = new SaveDocumentServiceImpl(templateGeneratorService, safeStorageService);
    }

    @Test
    void saveLegalFactShouldUploadPdfWithMetadataAndReturnPrefixedKey() {
        byte[] content = "legal-fact".getBytes(StandardCharsets.UTF_8);
        Map<String, List<String>> tags = Map.of("iun", List.of("IUN123"));

        when(safeStorageService.createAndUploadContent(any()))
                .thenReturn(new FileCreationResponseInt("generatedKey"));

        String result = saveDocumentService.saveLegalFact(content, tags);

        assertEquals("safestorage://generatedKey", result);

        ArgumentCaptor<FileCreationWithContentRequest> requestCaptor = ArgumentCaptor.forClass(FileCreationWithContentRequest.class);
        verify(safeStorageService).createAndUploadContent(requestCaptor.capture());
        FileCreationWithContentRequest req = requestCaptor.getValue();
        assertEquals(SaveDocumentServiceImpl.LEGALFACTS_MEDIATYPE_STRING, req.getContentType());
        assertEquals(SaveDocumentServiceImpl.PN_COMMUNICATIONS_COVERPAGE, req.getDocumentType());
        assertEquals(SaveDocumentServiceImpl.SAVED, req.getStatus());
        assertArrayEquals(content, req.getContent());
        assertEquals(tags, req.getTags());
    }

    @Test
    void saveLegalFactShouldReturnUnchangedKeyWhenStoragePrefixIsAlreadyPresent() {
        when(safeStorageService.createAndUploadContent(any()))
                .thenReturn(new FileCreationResponseInt("safestorage://generatedKey"));

        String result = saveDocumentService.saveLegalFact(new byte[0], Map.of());

        assertEquals("safestorage://generatedKey", result);
    }

    @Test
    void saveCoverpageShouldGenerateAndStoreCoverpageWithExpectedTags() throws Exception {
        byte[] fileBytes = "pdfcontent".getBytes(StandardCharsets.UTF_8);
        Path coverpagePath = tempDir.resolve("coverpage.pdf");
        Files.write(coverpagePath, fileBytes);
        File tmp = coverpagePath.toFile();

        NotificationInt notification = NotificationInt.builder().iun("iun-123").build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().taxId("tax-1").build();

        when(templateGeneratorService.informalAnalogCommunication(notification, recipient, true)).thenReturn(tmp);
        when(safeStorageService.createAndUploadContent(any())).thenReturn(new FileCreationResponseInt("k1"));

        String res = saveDocumentService.saveCoverpage(notification, recipient, "timeline-1", "0");

        assertEquals("safestorage://k1", res);

        ArgumentCaptor<FileCreationWithContentRequest> requestCaptor = ArgumentCaptor.forClass(FileCreationWithContentRequest.class);
        verify(safeStorageService).createAndUploadContent(requestCaptor.capture());
        FileCreationWithContentRequest req = requestCaptor.getValue();
        assertEquals(SaveDocumentServiceImpl.LEGALFACTS_MEDIATYPE_STRING, req.getContentType());
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
    void saveCoverpageShouldWrapFileReadFailuresInPnInternalException() {
        NotificationInt notification = NotificationInt.builder().iun("iun-999").build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().taxId("tax-999").build();
        File missingFile = tempDir.resolve("missing.pdf").toFile();

        when(templateGeneratorService.informalAnalogCommunication(notification, recipient, true)).thenReturn(missingFile);

        PnInternalException exc = assertThrows(PnInternalException.class, () ->
                saveDocumentService.saveCoverpage(notification, recipient, "t-1", "1")
        );

        assertEquals(String.format(SaveDocumentServiceImpl.SAVE_LEGAL_FACT_EXCEPTION_MESSAGE,
                "DIGITAL_DELIVERY", notification.getIun(), recipient.getTaxId()), exc.getProblem().getDetail());
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc.getProblem().getErrors().getFirst().getCode());
        assertNotNull(exc.getCause());
    }

    @Test
    void saveCoverpageShouldWrapStorageFailuresInPnInternalException() throws Exception {
        byte[] fileBytes = "pdfcontent".getBytes(StandardCharsets.UTF_8);
        Path coverpagePath = tempDir.resolve("coverpage-storage-failure.pdf");
        Files.write(coverpagePath, fileBytes);
        NotificationInt notification = NotificationInt.builder().iun("iun-999").build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder().taxId("tax-999").build();
        RuntimeException storageException = new RuntimeException("generation failed");

        when(templateGeneratorService.informalAnalogCommunication(notification, recipient, true)).thenReturn(coverpagePath.toFile());
        when(safeStorageService.createAndUploadContent(any())).thenThrow(storageException);

        PnInternalException exc = assertThrows(PnInternalException.class, () ->
                saveDocumentService.saveCoverpage(notification, recipient, "t-1", "1")
        );

        assertEquals(String.format(SaveDocumentServiceImpl.SAVE_LEGAL_FACT_EXCEPTION_MESSAGE,
                "DIGITAL_DELIVERY", notification.getIun(), recipient.getTaxId()), exc.getProblem().getDetail());
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc.getProblem().getErrors().getFirst().getCode());
        assertSame(storageException, exc.getCause());
    }
}
