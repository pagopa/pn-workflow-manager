package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.workflowmanager.action.utils.FileUtils.*;
import static it.pagopa.pn.workflowmanager.dto.action.common.ActionType.DOCUMENT_CREATION_RESPONSE;

@Component
@CustomLog
@AllArgsConstructor
public class SafeStorageHandler {

    private final SchedulerService schedulerService;

    public void handleSafeStorageResponse(FileDownloadResponse response) {
        final String processName = "SAFE_STORAGE_RESPONSE_HANDLER";
        try {
            log.logStartingProcess(processName);

            scheduleDocumentCreationResponse(response);

            log.logEndingProcess(processName);
        } catch (Exception ex) {
            log.logEndingProcess(processName, false, ex.getMessage(), ex);
            throw ex;
        }
    }

    private void scheduleDocumentCreationResponse(FileDownloadResponse response) {
        String iun = extractTagValue(response.getTags(), IUN_TAG);
        String recIndexStr = extractTagValue(response.getTags(), RECIPIENT_INDEX_TAG);
        Integer recIndex = recIndexStr != null ? Integer.parseInt(recIndexStr) : null;
        String documentType = extractTagValue(response.getTags(), DOCUMENT_TYPE_TAG);
        String elementId = extractTagValue(response.getTags(), TIMELINE_ELEMENT_ID_TAG);

        log.info("Scheduling DocumentCreationResponse - iun={} recIndex={} documentType={} elementId={}",
                iun, recIndex, documentType, elementId);

        DocumentCreationResponseActionDetails details = DocumentCreationResponseActionDetails.builder()
                .key(response.getKey())
                .documentCreationType(documentType)
                .timelineElementId(elementId)
                .build();

        Instant schedulingDate = Instant.now();

        log.info("Scheduling HandleDocumentCreationResponse schedulingDate={} - iun={} recIndex={} docType={}",
                schedulingDate, iun, recIndex, documentType);

        schedulerService.scheduleEvent(iun, recIndex, schedulingDate, DOCUMENT_CREATION_RESPONSE, details);

        log.info("DocumentCreationResponse scheduled successfully - iun={} recIndex={}", iun, recIndex);
    }

    private String extractTagValue(Map<String, List<String>> tags, String tagName) {
        if (tags == null) {
            log.warn("Tags map is null, cannot extract tag={}", tagName);
            return null;
        }
        List<String> values = tags.get(tagName);
        if (values == null || values.isEmpty()) {
            log.warn("Tag={} not found or empty in tags map", tagName);
            return null;
        }
        return values.getFirst();
    }
}

