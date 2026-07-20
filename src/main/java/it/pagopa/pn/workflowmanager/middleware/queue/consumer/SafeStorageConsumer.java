package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.SafeStorageHandler;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;

@Configuration
@CustomLog
@RequiredArgsConstructor
public class SafeStorageConsumer {

    private static final String SAFE_STORAGE_DOCUMENT_TYPE_PN_COMMUNICATIONS_COVERPAGE = "PN_COMMUNICATIONS_COVERPAGE";
    private static final String SAFE_STORAGE_CLIENT_NAME = "SAFE_STORAGE";

    private final SafeStorageHandler handler;

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

            handler.handleSafeStorageResponse(response);

            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
        } catch (Exception ex) {
            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
            MdcUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
