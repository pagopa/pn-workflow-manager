package it.pagopa.pn.workflowmanager.action.documentcreation;

import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DocumentCreationResponseHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaperChannelService paperChannelService;

    private DocumentCreationResponseHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final Integer TEST_REC_INDEX = 0;
    private static final String TEST_KEY = "coverpage-key-123";

    @BeforeEach
    void setup() {
        handler = new DocumentCreationResponseHandler(
                notificationService,
                paperChannelService
        );
    }

    @Test
    void handleResponseReceived_Success_WithCoverpage() {
        // GIVEN
        DocumentCreationResponseActionDetails actionDetails = DocumentCreationResponseActionDetails.builder()
                .key(TEST_KEY)
                .documentCreationType(DocumentCreationType.COVERPAGE.name())
                .build();

        NotificationInt notification = createMockNotification();

        Mockito.when(notificationService.getInformalNotificationByIun(TEST_IUN))
                .thenReturn(notification);

        Mockito.doNothing().when(paperChannelService).prepareSimpleRegisteredLetter(
                Mockito.any(NotificationInt.class),
                Mockito.anyInt(),
                Mockito.anyString()
        );

        // WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.handleResponseReceived(TEST_IUN, TEST_REC_INDEX, actionDetails)
        );

        // THEN
        Mockito.verify(notificationService, Mockito.times(1)).getInformalNotificationByIun(TEST_IUN);
        Mockito.verify(paperChannelService, Mockito.times(1)).prepareSimpleRegisteredLetter(
                notification,
                TEST_REC_INDEX,
                TEST_KEY
        );
    }

    @Test
    void handleResponseReceived_Failure_UnsupportedDocumentType() {
        // GIVEN
        DocumentCreationResponseActionDetails actionDetails = DocumentCreationResponseActionDetails.builder()
                .key(TEST_KEY)
                .documentCreationType("UNSUPPORTED_TYPE")
                .build();

        NotificationInt notification = createMockNotification();

        Mockito.when(notificationService.getInformalNotificationByIun(TEST_IUN))
                .thenReturn(notification);

        // WHEN & THEN
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleResponseReceived(TEST_IUN, TEST_REC_INDEX, actionDetails)
        );

        Assertions.assertTrue(exception.getMessage().contains("No enum constant"));
        Mockito.verify(notificationService, Mockito.times(1)).getInformalNotificationByIun(TEST_IUN);
        Mockito.verify(paperChannelService, Mockito.never()).prepareSimpleRegisteredLetter(
                Mockito.any(),
                Mockito.anyInt(),
                Mockito.anyString()
        );
    }

    private NotificationInt createMockNotification() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .taxId("TAXID001")
                .build();

        return NotificationInt.builder()
                .iun(TEST_IUN)
                .recipients(List.of(recipient))
                .build();
    }
}
