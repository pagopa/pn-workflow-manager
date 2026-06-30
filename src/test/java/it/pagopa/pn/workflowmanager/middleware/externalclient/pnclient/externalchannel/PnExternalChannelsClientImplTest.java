package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.LocalizedMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationMessageInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalLegalMessagesApi;
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
}