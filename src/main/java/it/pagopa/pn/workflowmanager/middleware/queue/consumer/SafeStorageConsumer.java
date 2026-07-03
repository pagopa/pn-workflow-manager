package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.workflowmanager.dto.action.common.ActionType.DOCUMENT_CREATION_RESPONSE;
import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class SafeStorageConsumer {

    private static final String SAFE_STORAGE_DOCUMENT_TYPE_PN_COMMUNICATIONS_COVERPAGE = "PN_COMMUNICATIONS_COVERPAGE";
    private static final String SAFE_STORAGE_CLIENT_NAME = "SAFE_STORAGE";

    private static final String TAG_IUN = "iun";
    private static final String TAG_REC_INDEX = "recIndex";
    private static final String TAG_DOCUMENT_TYPE = "documentType";
    private static final String TAG_ELEMENT_ID = "elementId";

    private final SchedulerService schedulerService;

    @SqsListener(queueNames = "#{@pnWorkflowManagerConfigs.topics.safeStorageEvents}")
    public void pnSafeStorageEventInboundConsumer(Message<FileDownloadResponse> message) {
        setMdc(message);
        try {
            log.info("Handle message from {} with content {}", SAFE_STORAGE_CLIENT_NAME, message);
            FileDownloadResponse response = message.getPayload();

            MDC.put(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY, response.getKey());

            if (!SAFE_STORAGE_DOCUMENT_TYPE_PN_COMMUNICATIONS_COVERPAGE.equals(response.getDocumentType())) {
                log.debug("Safe storage event received is not handled - documentType={}", response.getDocumentType());
                MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
                return;
            }

            scheduleDocumentCreationResponse(response);

            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
        } catch (Exception ex) {
            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
            MdcUtils.handleException(message.getHeaders(), ex);
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

        schedulerService.scheduleEvent(
                iun,
                recIndex,
                schedulingDate,
                DOCUMENT_CREATION_RESPONSE,
                details
        );

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
