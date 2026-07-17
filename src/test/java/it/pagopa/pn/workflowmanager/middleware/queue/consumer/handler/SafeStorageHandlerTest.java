package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SafeStorageHandlerTest {

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private SafeStorageHandler safeStorageHandler;

    @Captor
    private ArgumentCaptor<DocumentCreationResponseActionDetails> detailsCaptor;

    @Test
    void handleSafeStorageResponse_Success() {
        // Arrange
        Map<String, List<String>> tags = new HashMap<>();
        tags.put("iun", List.of("IUN-12345"));
        tags.put("recIndex", List.of("2"));
        tags.put("documentType", List.of("DIGITAL_SIGN"));
        tags.put("timelineElementId", List.of("elem-999"));

        FileDownloadResponse response = new FileDownloadResponse()
                .key("file-key-123")
                .documentType("PDF")
                .tags(tags);

        // Act
        safeStorageHandler.handleSafeStorageResponse(response);

        // Assert
        verify(schedulerService).scheduleEvent(
                eq("IUN-12345"),
                eq(2),
                any(Instant.class),
                eq(ActionType.DOCUMENT_CREATION_RESPONSE),
                detailsCaptor.capture()
        );

        // Verifica i dettagli dell'azione mappati
        DocumentCreationResponseActionDetails details = detailsCaptor.getValue();
        assertEquals("file-key-123", details.getKey());
        assertEquals("DIGITAL_SIGN", details.getDocumentCreationType());
        assertEquals("elem-999", details.getTimelineElementId());
    }

}