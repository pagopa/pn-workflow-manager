package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.api.IoConnectorApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.model.MessageRequest;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IoConnectorClientImplTest {
    @Mock
    private PnWorkflowManagerConfigs cfg;
    @Mock
    private IoConnectorApi ioConnectorApi;
    @InjectMocks
    private IoConnectorClientImpl ioConnectorClient;

    @BeforeEach
    void setUp() {
        when(cfg.getCxId()).thenReturn("cx-id");
        when(cfg.getIoPollingMaxMins()).thenReturn(5);
    }

    @Test
    void shouldSendMessageWithAttachmentsAndPaymentDataWhenRequestIsValid() {
        Instant dueDate = Instant.parse("2026-01-31T00:00:00Z");
        IoMessageRequest ioMessageRequest = buildIoMessageRequest(
                List.of(buildNotificationDocument("doc-key-1")),
                List.of(buildNotificationPaymentInfo(dueDate)));

        ioConnectorClient.sendMessage(ioMessageRequest);

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(ioConnectorApi).sendIOMessage(eq("cx-id"), captor.capture());

        MessageRequest sent = captor.getValue();
        assertEquals("req-1", sent.getRequestId());
        assertEquals("IUN_123", sent.getIun());
        assertEquals("TAXID123", sent.getRecipientTaxId());
        assertEquals("service-id", sent.getSenderServiceId());
        assertEquals("subject", sent.getSubject());
        assertEquals("This is a markdown message", sent.getMarkdown());
        assertEquals(Boolean.TRUE, sent.getSensitiveContent());

        assertNotNull(sent.getAttachments());
        assertEquals(1, sent.getAttachments().size());
        assertEquals("doc-key-1", sent.getAttachments().getFirst().getFileKey());
        assertEquals("doc-key-1", sent.getAttachments().getFirst().getId());
        assertEquals("doc-key-1", sent.getAttachments().getFirst().getName());

        assertEquals("2026-01-31T00:00:00Z", sent.getDueDate());
        assertNotNull(sent.getPaymentData());
        assertEquals(1000, sent.getPaymentData().getAmount());
        assertEquals("77777777777", sent.getPaymentData().getCreditorTaxId());
        assertEquals(Boolean.TRUE, sent.getPaymentData().getInvalidAfterDueDate());
        assertEquals("302012345678901234", sent.getPaymentData().getNoticeCode());
        assertEquals(5, sent.getPollingMaxMins());
    }

    @Test
    void shouldSendMessageWithoutPaymentDataWhenRecipientHasNoPayments() {
        IoMessageRequest ioMessageRequest = buildIoMessageRequest(
                List.of(),
                List.of()
        );

        ioConnectorClient.sendMessage(ioMessageRequest);

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(ioConnectorApi).sendIOMessage(eq("cx-id"), captor.capture());

        MessageRequest sent = captor.getValue();
        assertNull(sent.getAttachments());
        assertNull(sent.getDueDate());
        assertNull(sent.getPaymentData());
    }

    @Test
    void shouldUseFirstPaymentWhenRecipientHasMoreThanOnePayment() {
        IoMessageRequest ioMessageRequest = buildIoMessageRequest(
                List.of(buildNotificationDocument("doc-key-2")),
                List.of(
                        buildNotificationPaymentInfo(null),
                        buildNotificationPaymentInfo(Instant.now())
                )
        );

        ioConnectorClient.sendMessage(ioMessageRequest);

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(ioConnectorApi).sendIOMessage(eq("cx-id"), captor.capture());

        MessageRequest sent = captor.getValue();
        assertNull(sent.getDueDate());
        assertNotNull(sent.getPaymentData());
        assertEquals(false, sent.getPaymentData().getInvalidAfterDueDate());
        assertNotNull(sent.getPaymentData());
        assertEquals(1000, sent.getPaymentData().getAmount());
        assertEquals("77777777777", sent.getPaymentData().getCreditorTaxId());
        assertEquals("302012345678901234", sent.getPaymentData().getNoticeCode());
    }

    private IoMessageRequest buildIoMessageRequest(List<NotificationDocumentInt> docs, List<NotificationPaymentInfoInt> payments) {
        return IoMessageRequest.builder()
                .requestId("req-1")
                .markdown("This is a markdown message")
                .notificationInt(NotificationInt.builder()
                        .iun("IUN_123")
                        .documents(docs)
                        .build())
                .notificationRecipientInt(NotificationRecipientInt.builder()
                        .taxId("TAXID123")
                        .payments(payments)
                        .message(
                            NotificationMessageInt.builder()
                                .primaryMessage(
                                    LocalizedMessageInt.builder()
                                        .subject("subject")
                                        .longBody("long body")
                                        .language("IT")
                                        .build()
                                )
                            .build())
                        .build())
                .campaign(Campaign.builder()
                        .serviceId("service-id")
                        .sensitiveContent(true)
                        .build())
                .build();
    }

    private NotificationDocumentInt buildNotificationDocument(String key) {
        return NotificationDocumentInt.builder()
                .ref(NotificationDocumentInt.Ref.builder()
                        .key(key)
                        .build())
                .build();
    }

    private NotificationPaymentInfoInt buildNotificationPaymentInfo(Instant dueDate) {
        return NotificationPaymentInfoInt.builder()
                .pagoPA(PagoPaInt.builder()
                        .dueDate(dueDate)
                        .amount(1000)
                        .creditorTaxId("77777777777")
                        .noticeCode("302012345678901234")
                        .build())
                .build();
    }
}