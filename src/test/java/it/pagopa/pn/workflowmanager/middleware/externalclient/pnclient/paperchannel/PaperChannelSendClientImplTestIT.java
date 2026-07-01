package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.api.PaperMessagesApi;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.PrepareRequest;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.ProposalTypeEnum;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.ProductTypeEnum;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.SendRequest;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelSendRequest;
import it.pagopa.pn.workflowmanager.exceptions.PnPaperChannelChangedCostException;
import it.pagopa.pn.workflowmanager.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.workflowmanager.utils.NotificationTestBuilder;
import org.apache.http.HttpStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaperChannelSendClientImplTestIT {
    @Mock
    private PnWorkflowManagerConfigs cfg;
    @Mock
    private PaperMessagesApi paperMessagesApi;
    @InjectMocks
    private PaperMessagesClientImpl client;

    @Test
    void shouldBuildPrepareRequestFor890() {
        String requestId = "requestId";
        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder().build();
        NotificationInt notification = NotificationTestBuilder.builder().build();
        PaperChannelPrepareRequest paperChannelPrepareRequest = buildPrepareRequest(
                requestId,
                PhysicalAddressInt.ANALOG_TYPE.REGISTERED_LETTER_890,
                notification,
                recipient,
                null
        );

        client.prepare(paperChannelPrepareRequest);

        ArgumentCaptor<PrepareRequest> captor = ArgumentCaptor.forClass(PrepareRequest.class);
        verify(paperMessagesApi).sendPaperPrepareRequest(eq(requestId), captor.capture(), anyString());

        PrepareRequest sent = captor.getValue();
        assertEquals(requestId, sent.getRequestId());
        assertEquals(notification.getIun(), sent.getIun());
        assertEquals(ProposalTypeEnum._890, sent.getProposalProductType());
        assertEquals(recipient.getTaxId(), sent.getReceiverFiscalCode());
        assertEquals(recipient.getRecipientType().getValue(), sent.getReceiverType());
        assertEquals("test", sent.getReceiverAddress().getAddress());
        assertEquals(List.of("Att"), sent.getAttachmentUrls());
    }

    @Test
    void shouldBuildPrepareRequestForAR() {
        String requestId = "requestId";
        PaperChannelPrepareRequest paperChannelPrepareRequest = buildPrepareRequest(
                requestId,
                PhysicalAddressInt.ANALOG_TYPE.AR_REGISTERED_LETTER,
                NotificationTestBuilder.builder().build(),
                NotificationRecipientTestBuilder.builder().build(),
                null
        );

        client.prepare(paperChannelPrepareRequest);

        ArgumentCaptor<PrepareRequest> captor = ArgumentCaptor.forClass(PrepareRequest.class);
        verify(paperMessagesApi).sendPaperPrepareRequest(eq(requestId), captor.capture(), anyString());
        assertEquals(ProposalTypeEnum.AR, captor.getValue().getProposalProductType());
    }


    @Test
    void shouldBuildPrepareRequestForARSecondRequest() {
        String requestId = "requestId";
        String relatedRequestId = "requestId_0";
        PaperChannelPrepareRequest paperChannelPrepareRequest = buildPrepareRequest(
                requestId,
                PhysicalAddressInt.ANALOG_TYPE.AR_REGISTERED_LETTER,
                NotificationTestBuilder.builder().build(),
                NotificationRecipientTestBuilder.builder().build(),
                relatedRequestId
        );

        client.prepare(paperChannelPrepareRequest);

        ArgumentCaptor<PrepareRequest> captor = ArgumentCaptor.forClass(PrepareRequest.class);
        verify(paperMessagesApi).sendPaperPrepareRequest(eq(requestId), captor.capture(), anyString());
        assertEquals(relatedRequestId, captor.getValue().getRelatedRequestId());
    }

    @Test
    void shouldBuildPrepareRequestForSimpleRegisteredLetterWithNotificationSentAt() {
        String requestId = "requestId";
        NotificationInt notificationInt = NotificationTestBuilder.builder()
                .withSentAt(Instant.EPOCH.plusMillis(57))
                .withIun("iun_12345")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientTestBuilder.builder()
                .withTaxId("GeneratedTaxId_9ce24c59-862c-4024-aa75-40d888e6acac")
                .build();
        PaperChannelPrepareRequest paperChannelPrepareRequest = buildPrepareRequest(
                requestId,
                PhysicalAddressInt.ANALOG_TYPE.SIMPLE_REGISTERED_LETTER,
                notificationInt,
                recipient,
                null
        );

        client.prepare(paperChannelPrepareRequest);

        ArgumentCaptor<PrepareRequest> captor = ArgumentCaptor.forClass(PrepareRequest.class);
        verify(paperMessagesApi).sendPaperPrepareRequest(eq(requestId), captor.capture(), anyString());

        PrepareRequest sent = captor.getValue();
        assertEquals(ProposalTypeEnum.RS, sent.getProposalProductType());
        assertEquals(Instant.EPOCH.plusMillis(57), sent.getNotificationSentAt());
        assertEquals("iun_12345", sent.getIun());
        assertEquals("GeneratedTaxId_9ce24c59-862c-4024-aa75-40d888e6acac", sent.getReceiverFiscalCode());
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

    private PaperChannelPrepareRequest buildPrepareRequest(
            String requestId,
            PhysicalAddressInt.ANALOG_TYPE analogType,
            NotificationInt notificationInt,
            NotificationRecipientInt recipient,
            String relatedRequestId
    ) {
        return PaperChannelPrepareRequest.builder()
                .analogType(analogType)
                .requestId(requestId)
                .relatedRequestId(relatedRequestId)
                .paAddress(PhysicalAddressInt.builder()
                        .address("test")
                        .build())
                .recipientInt(recipient)
                .notificationInt(notificationInt)
                .attachments(List.of("Att"))
                .build();
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
                .recipientInt(NotificationRecipientTestBuilder.builder().build())
                .notificationInt(NotificationTestBuilder.builder().build())
                .attachments(List.of("Att"))
                .build();
    }
}