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

import static it.pagopa.pn.workflowmanager.dto.action.common.ActionType.DOCUMENT_CREATION_RESPONSE;

@Component
@CustomLog
@AllArgsConstructor
public class SafeStorageHandler {

    private static final String TAG_IUN = "iun";
    private static final String TAG_REC_INDEX = "recIndex";
    private static final String TAG_DOCUMENT_TYPE = "documentType";
    private static final String TAG_ELEMENT_ID = "elementId";

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
        String iun = extractTagValue(response.getTags(), TAG_IUN);
        String recIndexStr = extractTagValue(response.getTags(), TAG_REC_INDEX);
        Integer recIndex = recIndexStr != null ? Integer.parseInt(recIndexStr) : null;
        String documentType = extractTagValue(response.getTags(), TAG_DOCUMENT_TYPE);
        String elementId = extractTagValue(response.getTags(), TAG_ELEMENT_ID);

        log.info("Scheduling DocumentCreationResponse - iun={} recIndex={} documentType={} elementId={}",
                iun, recIndex, documentType, elementId);

        DocumentCreationResponseActionDetails details = DocumentCreationResponseActionDetails.builder()
                .key(response.getKey())
                .documentCreationType(response.getDocumentType())
                .iun(iun)
                .recIndex(recIndex)
                .documentType(documentType)
                .elementId(elementId)
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

