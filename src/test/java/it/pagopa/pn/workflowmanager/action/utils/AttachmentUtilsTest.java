package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.PagoPaInt;
import it.pagopa.pn.workflowmanager.utils.NotificationUtils;
import it.pagopa.pn.workflowmanager.utils.PnSendMode;
import it.pagopa.pn.workflowmanager.utils.PnSendModeUtils;
import it.pagopa.pn.workflowmanager.utils.SendAttachmentMode;
import it.pagopa.pn.workflowmanager.utils.TimelineUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class AttachmentUtilsTest {

    @Mock
    private PnSendModeUtils pnSendModeUtils;

    @Mock
    private NotificationUtils notificationUtils;

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private AttachmentUtils attachmentUtils;

    @Test
    void retrieveAttachmentsIncludesCoverpageBeforeDocumentsAndPayments() {
        int recIndex = 0;
        String iun = "IUN_123";

        NotificationDocumentInt document = NotificationDocumentInt.builder()
                .ref(NotificationDocumentInt.Ref.builder().key("document-key").build())
                .build();

        NotificationDocumentInt paymentAttachment = NotificationDocumentInt.builder()
                .ref(NotificationDocumentInt.Ref.builder().key("payment-key").build())
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .payments(List.of(NotificationPaymentInfoInt.builder()
                        .pagoPA(PagoPaInt.builder().attachment(paymentAttachment).build())
                        .build()))
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .documents(List.of(document))
                .recipients(List.of(recipient))
                .build();

        when(timelineUtils.retrieveCoverpageFileKey(iun, recIndex)).thenReturn("coverpage-key");
        when(notificationUtils.getRecipientFromIndex(notification, recIndex)).thenReturn(recipient);

        List<String> result = attachmentUtils.retrieveAttachments(
                notification,
                recIndex,
                new SendAttachmentMode(EnumSet.of(AttachmentType.COVERPAGE, AttachmentType.DOCUMENTS, AttachmentType.PAYMENTS)),
                true
        );

        assertEquals(List.of(
                "safestorage://coverpage-key?docTag=COVERPAGE",
                "safestorage://document-key?docTag=DOCUMENT",
                "safestorage://payment-key?docTag=ATTACHMENT_PAGOPA"
        ), result);
        verify(timelineUtils).retrieveCoverpageFileKey(iun, recIndex);
        verify(notificationUtils).getRecipientFromIndex(notification, recIndex);
    }

    @Test
    void retrieveAttachmentsSkipsInvalidPagoPaAttachmentsAndDoesNotAddDocTagWhenDisabled() {
        int recIndex = 0;

        NotificationDocumentInt document = NotificationDocumentInt.builder()
                .ref(NotificationDocumentInt.Ref.builder().key("document-key").build())
                .build();

        NotificationDocumentInt paymentAttachment = NotificationDocumentInt.builder()
                .ref(NotificationDocumentInt.Ref.builder().key("safestorage://payment-key").build())
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .payments(List.of(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder().attachment(paymentAttachment).build())
                                .build(),
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder().build())
                                .build(),
                        NotificationPaymentInfoInt.builder().build()
                ))
                .build();

        NotificationInt notification = NotificationInt.builder()
                .documents(List.of(document))
                .recipients(List.of(recipient))
                .build();

        when(notificationUtils.getRecipientFromIndex(notification, recIndex)).thenReturn(recipient);

        List<String> result = attachmentUtils.retrieveAttachments(
                notification,
                recIndex,
                new SendAttachmentMode(EnumSet.of(AttachmentType.DOCUMENTS, AttachmentType.PAYMENTS)),
                false
        );

        assertEquals(List.of(
                "safestorage://document-key",
                "safestorage://payment-key"
        ), result);
        verify(notificationUtils).getRecipientFromIndex(notification, recIndex);
        verifyNoInteractions(timelineUtils);
    }

    @Test
    void retrieveAttachmentTypesToSendReturnsConfiguredModeForEmailChannel() {
        SendAttachmentMode expectedMode = new SendAttachmentMode(EnumSet.of(AttachmentType.COVERPAGE, AttachmentType.DOCUMENTS));
        NotificationInt notification = NotificationInt.builder()
                .sentAt(Instant.parse("2024-01-10T10:15:30Z"))
                .build();

        when(pnSendModeUtils.getPnSendMode(notification.getSentAt()))
                .thenReturn(PnSendMode.builder().emailSendAttachmentMode(expectedMode).build());

        SendAttachmentMode result = attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.EMAIL);

        assertEquals(expectedMode, result);
    }

    @Test
    void retrieveAttachmentTypesToSendThrowsWhenConfigurationIsMissing() {
        Instant sentAt = Instant.parse("2024-01-10T10:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_123")
                .sentAt(sentAt)
                .build();

        when(pnSendModeUtils.getPnSendMode(sentAt)).thenReturn(null);

        PnInternalException exception = assertThrows(
                PnInternalException.class,
                () -> attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.EMAIL)
        );

        assertEquals(ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND,
                exception.getProblem().getErrors().getFirst().getCode());
    }

    @Test
    void retrieveAttachmentTypesToSendThrowsWhenChannelIsNotSupported() {
        Instant sentAt = Instant.parse("2024-01-10T10:15:30Z");
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_123")
                .sentAt(sentAt)
                .build();

        when(pnSendModeUtils.getPnSendMode(sentAt))
                .thenReturn(PnSendMode.builder()
                        .emailSendAttachmentMode(new SendAttachmentMode(EnumSet.of(AttachmentType.DOCUMENTS)))
                        .build());

        PnInternalException exception = assertThrows(
                PnInternalException.class,
                () -> attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.IO)
        );

        assertEquals(ERROR_CODE_WORKFLOWMANAGER_CONFIGURATION_NOT_FOUND,
                exception.getProblem().getErrors().getFirst().getCode());
    }
}
