package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.DocumentType;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.service.SafeStorageService;
import it.pagopa.pn.workflowmanager.service.SaveDocumentService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import it.pagopa.pn.workflowmanager.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED;


@Slf4j
@Service
@AllArgsConstructor
public class SaveDocumentServiceImpl implements SaveDocumentService {

    public static final String SAVE_DOCUMENT_EXCEPTION_MESSAGE = "Generating %s document for IUN=%s and recipientId=%d";
    public static final String DOCUMENTS_MEDIATYPE_STRING = "application/pdf";
    public static final String PN_COMMUNICATIONS_COVERPAGE = "PN_COMMUNICATIONS_COVERPAGE";
    public static final String SAVED = "SAVED";

    private final TemplateGeneratorService templateGeneratorService;

    private final SafeStorageService safeStorageService;

    public String saveDocument(byte[] content, Map<String, List<String>> tags) {
        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        fileCreationRequest.setContentType(DOCUMENTS_MEDIATYPE_STRING);
        fileCreationRequest.setDocumentType(PN_COMMUNICATIONS_COVERPAGE);
        fileCreationRequest.setStatus(SAVED);
        fileCreationRequest.setContent(content);
        fileCreationRequest.setTags(tags);
        
        return FileUtils.getKeyWithStoragePrefix(
                safeStorageService.createAndUploadContent(fileCreationRequest).getKey()
        );
    }

    public String saveCoverpage(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            Campaign campaign,
            String timelineElementId,
            int recIndex
    ) {
        try {
            log.debug("Start saveCoverpage - iun={}", notification.getIun());
            File legalFactFile = templateGeneratorService.generateCoverpageTemplate(notification, recipient, campaign);
            byte[] legalFact = Files.readAllBytes(legalFactFile.toPath());
            Map<String, List<String>> tags = Map.of(
                    "iun", List.of(notification.getIun()),
                    "recIndex", List.of(String.valueOf(recIndex)),
                    "documentType", List.of(DocumentType.COVERPAGE.name()),
                    "timelineElementId", List.of(timelineElementId)
            );
            return saveDocument(legalFact, tags);
        } catch (Exception exc) {
            String msg = String.format(SAVE_DOCUMENT_EXCEPTION_MESSAGE, "COVERPAGE", notification.getIun(), recIndex);
            throw new PnInternalException(msg, ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc);
        }
    }
}
