package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelSendRequest;
import it.pagopa.pn.workflowmanager.exceptions.PnPaperChannelChangedCostException;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.api.InformalMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.api.PaperMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.*;
import it.pagopa.pn.workflowmanager.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.workflowmanager.utils.NotificationTestBuilder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel.PaperMessagesClient.PRINT_TYPE_BN_FRONTE_RETRO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaperChannelSendClientImplTest {
    private static final String TEST_CX_ID = "test-cx-id";

    @Mock
    private PnWorkflowManagerConfigs cfg;
    @Mock
    private InformalMessagesApi informalMessagesApi;
    @Mock
    private PaperMessagesApi paperMessagesApi;
    @InjectMocks
    private PaperMessagesClientImpl client;

    @Test
    void shouldBuildPrepareRequestForSimpleRegisteredLetterWithNotificationSentAt() {
        when(cfg.getCxId()).thenReturn(TEST_CX_ID);

        String TEST_IUN = "iun_12345";
        Instant SENT_AT = Instant.EPOCH.plusMillis(57);
        NotificationInt notificationInt = NotificationTestBuilder.builder()
                .withSentAt(SENT_AT)
                .withIun(TEST_IUN)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("GeneratedTaxId_9ce24c59-862c-4024-aa75-40d888e6acac")
                .build();

        String TEST_REQUEST_ID = "requestId_12345";
        PaperChannelPrepareRequest paperChannelPrepareRequest = PaperChannelPrepareRequest.builder()
                .analogType(PhysicalAddressInt.ANALOG_TYPE.SIMPLE_REGISTERED_LETTER)
                .requestId(TEST_REQUEST_ID)
                .paAddress(PhysicalAddressInt.builder()
                        .address("test")
                        .build())
                .recipientInt(recipient)
                .notificationInt(notificationInt)
                .attachments(List.of("Att"))
                .build();

        client.prepare(paperChannelPrepareRequest);

        ArgumentCaptor<InformalPrepareRequest> captor = ArgumentCaptor.forClass(InformalPrepareRequest.class);
        verify(informalMessagesApi).sendInformalPrepareRequest(captor.capture(), eq(TEST_CX_ID));

        InformalPrepareRequest sent = captor.getValue();
        assertEquals(TEST_REQUEST_ID, sent.getRequestId());
        assertEquals(TEST_IUN, sent.getIun());
        assertEquals(PRINT_TYPE_BN_FRONTE_RETRO, sent.getPrintType());
        assertEquals(InformalProposalProductTypeEnum.RS, sent.getProposalProductType());
        assertNotNull(sent.getReceiverAddress());
        String maskedAddress = sent.getReceiverAddress().getAddress();
        assertEquals("test", maskedAddress);
        assertEquals(paperChannelPrepareRequest.getAttachments(), sent.getAttachmentUrls());
        assertEquals(recipient.getRecipientType().getValue(), sent.getReceiverType());
        assertEquals(notificationInt.getSender().getPaId(), sent.getSenderPaId());
        assertEquals(SENT_AT, sent.getNotificationSentAt());
    }

    @Test
    void shouldSendAndReturnResponseAmount() {
        String requestId = "requestId";

        SendResponse response = new SendResponse();
        int notificationCostExpected = 100;
        response.setAmount(notificationCostExpected);
        when(paperMessagesApi.sendPaperSendRequest(eq(requestId), any(SendRequest.class))).thenReturn(response);

        PaperChannelSendRequest paperChannelSendRequest = buildSendRequest(requestId);

        SendResponse sendResponse = client.send(paperChannelSendRequest);
        assertEquals(notificationCostExpected, sendResponse.getAmount());

        ArgumentCaptor<SendRequest> captor = ArgumentCaptor.forClass(SendRequest.class);
        verify(paperMessagesApi).sendPaperSendRequest(eq(requestId), captor.capture());
        SendRequest sent = captor.getValue();
        assertEquals(requestId, sent.getRequestId());
        assertEquals(ProductTypeEnum._890, sent.getProductType());
        assertEquals("test", sent.getArAddress().getAddress());
        assertEquals("test2", sent.getReceiverAddress().getAddress());
        assertEquals(List.of("Att"), sent.getAttachmentUrls());
        assertNotNull(sent.getClientRequestTimeStamp());
    }

    @Test
    void shouldThrowChangedCostExceptionWhenUnprocessableErrorOccurs() {
        String requestId = "requestId1";
        PaperChannelSendRequest paperChannelSendRequest = buildSendRequest(requestId);
        PnHttpResponseException exception = new PnHttpResponseException("unprocessable", HttpStatus.SC_UNPROCESSABLE_ENTITY);

        when(paperMessagesApi.sendPaperSendRequest(eq(requestId), any(SendRequest.class)))
                .thenThrow(exception);

        assertThrows(PnPaperChannelChangedCostException.class, () -> client.send(paperChannelSendRequest));
    }

    @Test
    void shouldRethrowPnHttpResponseExceptionWhenGenericErrorOccurs() {
        String requestId = "requestId2";
        PaperChannelSendRequest paperChannelSendRequest = buildSendRequest(requestId);
        PnHttpResponseException exception = new PnHttpResponseException("generic error", HttpStatus.SC_INTERNAL_SERVER_ERROR);

        when(paperMessagesApi.sendPaperSendRequest(eq(requestId), any(SendRequest.class)))
                .thenThrow(exception);

        PnHttpResponseException thrown = assertThrows(PnHttpResponseException.class, () -> client.send(paperChannelSendRequest));
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    private PaperChannelSendRequest buildSendRequest(String requestId) {
        return PaperChannelSendRequest.builder()
                .requestId(requestId)
                .productType(ProductTypeEnum._890.getValue())
                .arAddress(PhysicalAddressInt.builder()
                        .address("test")
                        .build())
                .receiverAddress(PhysicalAddressInt.builder()
                        .address("test2")
                        .build())
                .senderAddress(PhysicalAddressInt.builder()
                        .address("sender-address")
                        .build())
                .recipientInt(NotificationRecipientTestBuilder.builder().build())
                .notificationInt(NotificationTestBuilder.builder().build())
                .attachments(List.of("Att"))
                .build();
    }
}