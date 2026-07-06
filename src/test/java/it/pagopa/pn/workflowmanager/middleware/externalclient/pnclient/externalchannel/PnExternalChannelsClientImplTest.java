package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.LocalizedMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalCourtesyMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalLegalMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalNotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PnExternalChannelsClientImplTest {
    @Mock
    private PnWorkflowManagerConfigs cfg;

    @Mock
    private DigitalLegalMessagesApi digitalLegalMessagesApi;

    @Mock
    private DigitalCourtesyMessagesApi digitalCourtesyMessagesApi;

    @InjectMocks
    private PnExternalChannelsClientImpl client;

    @Test
    void sendsPecNotificationWithExpectedPayloadAndNormalizedAttachmentUrls() {
        String requestId = "request-id";
        String cxId = "cx-id";
        String mailBody = "<p>body</p>";
        String subject = "subject";
        String pecAddress = "receiver@pec.it";

        NotificationInt notification = mock(NotificationInt.class);

        LegalDigitalAddressInt digitalAddress = mock(LegalDigitalAddressInt.class);
        when(digitalAddress.getAddress()).thenReturn(pecAddress);

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject(subject)
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        when(cfg.getCxId()).thenReturn(cxId);

        client.sendNotificationPEC(
                requestId,
                mailBody,
                notification,
                recipient,
                digitalAddress,
                List.of("file-1", "safestorage://file-2")
        );

        ArgumentCaptor<DigitalNotificationRequest> requestCaptor = ArgumentCaptor.forClass(DigitalNotificationRequest.class);
        verify(digitalLegalMessagesApi).sendDigitalLegalMessage(eq(requestId), eq(cxId), requestCaptor.capture());

        DigitalNotificationRequest sent = requestCaptor.getValue();
        assertEquals(DigitalNotificationRequest.ChannelEnum.PEC, sent.getChannel());
        assertEquals(requestId, sent.getRequestId());
        assertEquals(requestId, sent.getCorrelationId());
        assertEquals("INFORMAL", sent.getEventType());
        assertEquals(DigitalNotificationRequest.MessageContentTypeEnum.TEXT_HTML, sent.getMessageContentType());
        assertEquals(DigitalNotificationRequest.QosEnum.BATCH, sent.getQos());
        assertEquals(pecAddress, sent.getReceiverDigitalAddress());
        assertEquals(mailBody, sent.getMessageText());
        assertEquals(subject, sent.getSubjectText());
        assertNotNull(sent.getClientRequestTimeStamp());
        assertEquals(List.of("safestorage://file-1", "safestorage://file-2"), sent.getAttachmentUrls());
    }

    @Test
    void sendsPecNotificationWithEmptyAttachmentsWhenFileKeysIsEmpty() {
        String requestId = "request-id";
        String cxId = "cx-id";

        NotificationInt notification = mock(NotificationInt.class);

        LegalDigitalAddressInt digitalAddress = mock(LegalDigitalAddressInt.class);
        when(digitalAddress.getAddress()).thenReturn("receiver@pec.it");

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        when(cfg.getCxId()).thenReturn(cxId);

        client.sendNotificationPEC(requestId, "body", notification, recipient, digitalAddress, List.of());

        ArgumentCaptor<DigitalNotificationRequest> requestCaptor = ArgumentCaptor.forClass(DigitalNotificationRequest.class);
        verify(digitalLegalMessagesApi).sendDigitalLegalMessage(eq(requestId), eq(cxId), requestCaptor.capture());
        assertEquals(List.of(), requestCaptor.getValue().getAttachmentUrls());
    }

    @Test
    void wrapsAndRethrowsWhenExternalApiFails() {
        String requestId = "request-id";
        RuntimeException apiException = new RuntimeException("boom");

        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("IUN12345");

        LegalDigitalAddressInt digitalAddress = mock(LegalDigitalAddressInt.class);
        when(digitalAddress.getAddress()).thenReturn("receiver@pec.it");

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        when(cfg.getCxId()).thenReturn("cx-id");
        doThrow(apiException).when(digitalLegalMessagesApi)
                .sendDigitalLegalMessage(anyString(), anyString(), any(DigitalNotificationRequest.class));

        PnInternalException thrown = assertThrows(
                PnInternalException.class,
                () -> client.sendNotificationPEC(requestId, "body", notification, recipient, digitalAddress, List.of("file"))
        );

        assertSame(apiException, thrown.getCause());
    }

    @Test
    void wrapsAndRethrowsWhenFileKeysIsNull() {
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("IUN12345");

        LegalDigitalAddressInt digitalAddress = mock(LegalDigitalAddressInt.class);

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        assertThrows(
                PnInternalException.class,
                () -> client.sendNotificationPEC("request-id", "body", notification, recipient, digitalAddress, null)
        );
    }

    @Test
    void sendNotificationEMAILWithExpectedPayload() {
        String requestId = "request-id";
        String cxId = "cx-id";
        String mailBody = "<p>body</p>";
        String subject = "subject";
        String pecAddress = "receiver@example.it";

        NotificationInt notification = mock(NotificationInt.class);

        DigitalAddressInt digitalAddress = mock(DigitalAddressInt.class);
        when(digitalAddress.getAddress()).thenReturn(pecAddress);

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject(subject)
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        when(cfg.getCxId()).thenReturn(cxId);

        client.sendNotificationEMAIL(
                requestId,
                mailBody,
                notification,
                recipient,
                digitalAddress,
                List.of("aarKey")
        );

        ArgumentCaptor<DigitalCourtesyMailRequest> requestCaptor = ArgumentCaptor.forClass(DigitalCourtesyMailRequest.class);
        verify(digitalCourtesyMessagesApi).sendDigitalCourtesyMessage(eq(requestId), eq(cxId), requestCaptor.capture());

        DigitalCourtesyMailRequest sent = requestCaptor.getValue();
        assertEquals(requestId, sent.getRequestId());
        assertEquals(requestId, sent.getCorrelationId());
        assertEquals("INFORMAL", sent.getEventType());
        assertEquals(DigitalCourtesyMailRequest.MessageContentTypeEnum.TEXT_HTML, sent.getMessageContentType());
        assertEquals(DigitalCourtesyMailRequest.QosEnum.BATCH, sent.getQos());
        assertEquals(pecAddress, sent.getReceiverDigitalAddress());
        assertEquals(mailBody, sent.getMessageText());
        assertEquals(subject, sent.getSubjectText());
        assertNotNull(sent.getClientRequestTimeStamp());
        assertEquals(List.of("safestorage://aarKey"), sent.getAttachmentUrls());
    }

    @Test
    void wrapsWhenSendNotificationEmailFailAndRethrowsWhenExternalApiFails() {
        String requestId = "request-id";
        RuntimeException apiException = new RuntimeException("boom");

        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn("IUN12345");

        DigitalAddressInt digitalAddress = mock(DigitalAddressInt.class);
        when(digitalAddress.getAddress()).thenReturn("receiver@example.it");

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .message(NotificationMessageInt.builder()
                        .primaryMessage(LocalizedMessageInt.builder()
                                .subject("subject")
                                .longBody("long-body")
                                .language("it")
                                .build())
                        .build())
                .build();

        when(cfg.getCxId()).thenReturn("cx-id");
        doThrow(apiException).when(digitalCourtesyMessagesApi)
                .sendDigitalCourtesyMessage(anyString(), anyString(), any(DigitalCourtesyMailRequest.class));

        PnInternalException thrown = assertThrows(
                PnInternalException.class,
                () -> client.sendNotificationEMAIL(requestId, "body", notification, recipient, digitalAddress, List.of("aarKey"))
        );

        assertSame(apiException, thrown.getCause());
    }

    @Test
    void sendsSmsNotificationWithExpectedPayload() {
        String requestIdx = "request-idx";
        String cxId = "cx-id";
        String textMessage = "Hello SMS";
        String senderDigitalAddress = "+39123456789";

        when(cfg.getCxId()).thenReturn(cxId);

        client.sendNotificationSMS(requestIdx, textMessage, senderDigitalAddress);

        ArgumentCaptor<DigitalCourtesySmsRequest> requestCaptor = ArgumentCaptor.forClass(DigitalCourtesySmsRequest.class);
        verify(digitalCourtesyMessagesApi).sendCourtesyShortMessage(eq(requestIdx), eq(cxId), requestCaptor.capture());

        DigitalCourtesySmsRequest sent = requestCaptor.getValue();
        assertEquals(DigitalCourtesySmsRequest.ChannelEnum.SMS, sent.getChannel());
        assertEquals(requestIdx, sent.getRequestId());
        assertEquals(requestIdx, sent.getCorrelationId());
        assertEquals("INFORMAL", sent.getEventType());
        assertEquals(DigitalCourtesySmsRequest.QosEnum.BATCH, sent.getQos());
        assertEquals(senderDigitalAddress, sent.getReceiverDigitalAddress());
        assertEquals(textMessage, sent.getMessageText());
        assertNotNull(sent.getClientRequestTimeStamp());
    }

    @Test
    void wrapsAndRethrowsWhenSmsApiFails() {
        String requestIdx = "request-idx";
        RuntimeException apiException = new RuntimeException("boom");

        when(cfg.getCxId()).thenReturn("cx-id");
        doThrow(apiException).when(digitalCourtesyMessagesApi)
                .sendCourtesyShortMessage(anyString(), anyString(), any(DigitalCourtesySmsRequest.class));

        PnInternalException thrown = assertThrows(
                PnInternalException.class,
                () -> client.sendNotificationSMS(requestIdx, "text", "+39123456789")
        );

        assertSame(apiException, thrown.getCause());
    }
}