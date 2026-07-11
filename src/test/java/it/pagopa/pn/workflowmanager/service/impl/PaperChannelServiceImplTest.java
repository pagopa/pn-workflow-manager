package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel.PaperMessagesClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaperChannelServiceImplTest {

    @Mock
    private PaperMessagesClient paperMessagesClient;

    @Mock
    private PaperChannelUtils paperChannelUtils;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ChannelSenderUtils channelSenderUtils;

    private PaperChannelServiceImpl service;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        service = new PaperChannelServiceImpl(
                paperMessagesClient,
                auditLogService,
                paperChannelUtils,
                channelSenderUtils
        );
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void prepareSimpleRegisteredLetter_Success() {
        // GIVEN
        String iun = "TEST-IUN-001";
        Integer recIndex = 0;
        String coverpageFileKey = "coverpage-key";

        NotificationInt notification = createNotification(iun);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(paperChannelUtils.retrieveAttachmentsToSend(any(), eq(recIndex)))
                .thenReturn(List.of("attachment1"));

        when(auditLogService.buildAuditLogEvent(eq(iun), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_PD_PREPARE),
                anyString(), anyString()))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        doNothing().when(paperMessagesClient).prepare(any(PaperChannelPrepareRequest.class));
        doNothing().when(channelSenderUtils).savePrepareAnalogDeliveryElement(
                anyInt(), any(), anyString(), any(), anyInt(), any(), any()
        );

        // WHEN
        Assertions.assertDoesNotThrow(() ->
                service.prepareSimpleRegisteredLetter(notification, recIndex, coverpageFileKey)
        );

        // THEN
        verify(paperMessagesClient, times(1)).prepare(any(PaperChannelPrepareRequest.class));
        verify(channelSenderUtils, times(1)).savePrepareAnalogDeliveryElement(
                anyInt(), any(), anyString(), any(), anyInt(), any(), any()
        );
        verify(auditLogEvent, times(1)).generateSuccess(anyString());
        verify(auditLogEvent, times(1)).log();
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void prepareSimpleRegisteredLetter_Failure() {
        // GIVEN
        String iun = "TEST-IUN-002";
        Integer recIndex = 0;
        String coverpageFileKey = "coverpage-key";

        NotificationInt notification = createNotification(iun);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(paperChannelUtils.retrieveAttachmentsToSend(any(), eq(recIndex)))
                .thenReturn(List.of("attachment1"));

        when(auditLogService.buildAuditLogEvent(eq(iun), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_PD_PREPARE),
                anyString(), anyString()))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateFailure(anyString(), any())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        doThrow(new RuntimeException("Paper channel error"))
                .when(paperMessagesClient).prepare(any(PaperChannelPrepareRequest.class));

        Assertions.assertThrows(PnInternalException.class, () ->
                service.prepareSimpleRegisteredLetter(notification, recIndex, coverpageFileKey)
        );

        verify(paperMessagesClient, times(1)).prepare(any(PaperChannelPrepareRequest.class));
        verify(channelSenderUtils, never()).savePrepareAnalogDeliveryElement(
                anyInt(), any(), anyString(), any(), anyInt(), any(), any()
        );
        verify(auditLogEvent, times(1)).generateFailure(anyString(), any());
        verify(auditLogEvent, times(1)).log();
    }

    @ExtendWith(MockitoExtension.class)
    @Test
    void sendSimpleRegisteredLetter_Failure() {
        // GIVEN
        String iun = "TEST-IUN-004";
        Integer recIndex = 0;
        String prepareRequestId = "PREPARE-REQUEST-002";
        String productType = "AR_REGISTERED_LETTER";

        NotificationInt notification = createNotification(iun);
        PhysicalAddressInt receiverAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .fullname("Mario Rossi")
                .build();

        List<String> replacedF24AttachmentUrls = List.of();
        CategorizedAttachmentsResultInt categorizedAttachmentsResult = CategorizedAttachmentsResultInt.builder()
                .acceptedAttachments(new ArrayList<>())
                .discardedAttachments(new ArrayList<>())
                .build();

        PnAuditLogEvent auditLogEvent = Mockito.mock(PnAuditLogEvent.class);
        PhysicalAddressInt senderAddress = PhysicalAddressInt.builder().address("Via Sender").build();

        Mockito.when(auditLogService.buildAuditLogEvent(Mockito.anyString(), Mockito.anyInt(),
                        Mockito.eq(PnAuditLogEventType.AUD_COM_PD_EXECUTE), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.generateFailure(Mockito.anyString(), Mockito.any())).thenReturn(auditLogEvent);
        Mockito.when(auditLogEvent.log()).thenReturn(auditLogEvent);

        Mockito.when(paperChannelUtils.getSenderAddress()).thenReturn(senderAddress);
        Mockito.when(paperMessagesClient.send(Mockito.any())).thenThrow(new RuntimeException("Send failed"));

        // WHEN & THEN
        Assertions.assertThrows(RuntimeException.class, () ->
                service.sendSimpleRegisteredLetter(
                        notification, recIndex, prepareRequestId, receiverAddress,
                        productType, replacedF24AttachmentUrls, categorizedAttachmentsResult
                )
        );

        Mockito.verify(paperMessagesClient).send(Mockito.any());
        Mockito.verify(paperChannelUtils, Mockito.never()).addSendAnalogNotificationToTimeline(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(auditLogEvent).generateFailure(Mockito.anyString(), Mockito.any());
        Mockito.verify(auditLogEvent).log();
    }

    private NotificationInt createNotification(String iun) {
        PhysicalAddressInt physicalAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .physicalAddress(physicalAddress)
                .build();

        return NotificationInt.builder()
                .iun(iun)
                .recipients(List.of(recipient))
                .build();
    }

}
