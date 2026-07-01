package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.safestorage.DocumentType;
import it.pagopa.pn.workflowmanager.dto.safestorage.FileCreationWithContentRequest;
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

    public static final String SAVE_LEGAL_FACT_EXCEPTION_MESSAGE = "Generating %s legal fact for IUN=%s and recipientId=%s";
    public static final String LEGALFACTS_MEDIATYPE_STRING = "application/pdf";
    public static final String PN_COMMUNICATIONS_COVERPAGE = "PN_COMMUNICATIONS_COVERPAGE";
    public static final String SAVED = "SAVED";

    private final TemplateGeneratorService templateGeneratorService;

    private final SafeStorageService safeStorageService;

    public String saveLegalFact(byte[] legalFact, Map<String, List<String>> tags) {
        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_STRING);
        fileCreationRequest.setDocumentType(PN_COMMUNICATIONS_COVERPAGE);
        fileCreationRequest.setStatus(SAVED);
        fileCreationRequest.setContent(legalFact);
        fileCreationRequest.setTags(tags);
        
        return FileUtils.getKeyWithStoragePrefix(
                safeStorageService.createAndUploadContent(fileCreationRequest).getKey()
        );
    }

    public String saveCoverpage(
            NotificationInt notification,
            NotificationRecipientInt recipient,
            String timelineElementId,
            String recIndex
    ) {
        try {
            log.debug("Start saveCoverpage - iun={}", notification.getIun());
            File legalFactFile = templateGeneratorService.informalAnalogCommunication(notification, recipient, true);
            byte[] legalFact = Files.readAllBytes(legalFactFile.toPath());
            Map<String, List<String>> tags = Map.of(
                    "iun", List.of(notification.getIun()),
                    "recIndex", List.of(recIndex),
                    "documentType", List.of(DocumentType.COVERPAGE.name()),
                    "timelineElementId", List.of(timelineElementId)
            );
            return saveLegalFact(legalFact, tags);
        } catch (Exception exc) {
            String msg = String.format(SAVE_LEGAL_FACT_EXCEPTION_MESSAGE, "DIGITAL_DELIVERY", notification.getIun(), recipient.getTaxId());
            throw new PnInternalException(msg, ERROR_CODE_WORKFLOWMANAGER_SAVELEGALFACTSFAILED, exc);
        }
    }
}
