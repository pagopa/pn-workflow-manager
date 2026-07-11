package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.*;
import it.pagopa.pn.workflowmanager.action.analogworkflow.AnalogWorkflowPaperChannelResponseHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog.AnalogEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.PrepareEventInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


class PaperChannelHandlerTest {

    private AnalogWorkflowPaperChannelResponseHandler analogWorkflowPaperChannelResponseHandler;

    private TimelineUtils timelineUtils;

    private PaperChannelHandler handler;

    private ChannelEventProcessor channelEventProcessor;

    private AnalogEventNormalizer analogEventNormalizer;


    @BeforeEach
    void setup() {
        channelEventProcessor = Mockito.mock(ChannelEventProcessor.class);
        analogEventNormalizer = Mockito.mock(AnalogEventNormalizer.class);
        timelineUtils = Mockito.mock(TimelineUtils.class);
        analogWorkflowPaperChannelResponseHandler = Mockito.mock(AnalogWorkflowPaperChannelResponseHandler.class);

        handler = new PaperChannelHandler(
                analogWorkflowPaperChannelResponseHandler,
                timelineUtils,
                channelEventProcessor,
                analogEventNormalizer
        );
    }

    @Test
    void prepareUpdateTest_OK() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.OK);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("ok");
        prepareEvent.setReceiverAddress(new AnalogAddress());
        prepareEvent.setReplacedF24AttachmentUrls(List.of("replacedF24Urls"));
        prepareEvent.setCategorizedAttachments(new CategorizedAttachmentsResult());
        Assertions.assertNotNull(prepareEvent.getCategorizedAttachments());
        prepareEvent.getCategorizedAttachments().setAcceptedAttachments(new ArrayList<>());
        Assertions.assertNotNull(prepareEvent.getCategorizedAttachments().getAcceptedAttachments());
        prepareEvent.getCategorizedAttachments().getAcceptedAttachments().add(new ResultFilter()
                .fileKey("fileKey")
                .result(ResultFilterEnum.SUCCESS)
                .reasonCode("getReasonCode")
                .reasonDescription("getReasonDescription"));
        prepareEvent.getCategorizedAttachments().setDiscardedAttachments(new ArrayList<>());
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        handler.paperChannelResponseReceiver(singleStatusUpdate);

        PrepareEventInt tmp = PrepareEventInt.builder()
                .iun("iun_event_idx_0")
                .requestId("iun_event_idx_0")
                .statusCode("OK")
                .statusDateTime(instant)
                .statusDetail("ok")
                .replacedF24AttachmentUrls(List.of("replacedF24Urls"))
                .categorizedAttachmentsResult(CategorizedAttachmentsResultInt.builder()
                        .acceptedAttachments(List.of(ResultFilterInt.builder()
                                .fileKey("fileKey")
                                .result(it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterEnum.fromValue(ResultFilterEnum.SUCCESS.getValue()))
                                .reasonCode("getReasonCode")
                                .reasonDescription("getReasonDescription")
                                .build()))
                        .discardedAttachments(new ArrayList<>())
                        .build()
                )
                .receiverAddress(new PhysicalAddressInt())
                .build();

        Mockito.verify(analogWorkflowPaperChannelResponseHandler, Mockito.times(1)).paperChannelPrepareResponseHandler(tmp);
    }

    @Test
    void prepareUpdateTest_KO_1() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.KO);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("ko");
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D00);
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        handler.paperChannelResponseReceiver(singleStatusUpdate);

        PrepareEventInt tmp = PrepareEventInt.builder()
                .iun("iun_event_idx_0")
                .requestId("iun_event_idx_0")
                .statusCode("KO")
                .statusDateTime(instant)
                .failureDetailCode(FailureDetailCodeEnum.D00.getValue())
                .statusDetail("ko")
                .build();

        Mockito.verify(analogWorkflowPaperChannelResponseHandler, Mockito.times(1)).paperChannelPrepareResponseHandler(tmp);
    }

    @Test
    void prepareUpdateTest_KO_2() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");
        PrepareEvent prepareEvent = getPrepareEvent(instant);
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D01);
        Assertions.assertNotNull(prepareEvent.getReceiverAddress());
        prepareEvent.getReceiverAddress().setAddress("via prova 123");
        prepareEvent.getReceiverAddress().setCap("32323");
        prepareEvent.getReceiverAddress().setCountry("italia");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        handler.paperChannelResponseReceiver(singleStatusUpdate);

        PrepareEventInt tmp = PrepareEventInt.builder()
                .iun("iun_event_idx_0")
                .requestId("iun_event_idx_0")
                .statusCode("KO")
                .statusDateTime(instant)
                .failureDetailCode(FailureDetailCodeEnum.D01.getValue())
                .receiverAddress(PhysicalAddressInt.builder()
                        .address(prepareEvent.getReceiverAddress().getAddress())
                        .zip(prepareEvent.getReceiverAddress().getCap())
                        .foreignState(prepareEvent.getReceiverAddress().getCountry())
                        .build())
                .statusDetail("ko")
                .build();

        Mockito.verify(analogWorkflowPaperChannelResponseHandler, Mockito.times(1)).paperChannelPrepareResponseHandler(tmp);
    }

    private PrepareEvent getPrepareEvent(Instant instant) {
        AnalogAddress analogAddress = new AnalogAddress();
        analogAddress.setAddress("address");
        analogAddress.setCap("cap");
        analogAddress.setCountry("country");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.KO);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("ko");
        prepareEvent.setReceiverAddress(analogAddress);
        return prepareEvent;
    }

    @Test
    void prepareUpdateTest_KO_3() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");
        PrepareEvent prepareEvent = getPrepareEvent(instant);
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D02);
        Assertions.assertNotNull(prepareEvent.getReceiverAddress());
        prepareEvent.getReceiverAddress().setAddress("via prova 123");
        prepareEvent.getReceiverAddress().setCap("32323");
        prepareEvent.getReceiverAddress().setCountry("italia");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        handler.paperChannelResponseReceiver(singleStatusUpdate);

        PrepareEventInt tmp = PrepareEventInt.builder()
                .iun("iun_event_idx_0")
                .requestId("iun_event_idx_0")
                .statusCode("KO")
                .statusDateTime(instant)
                .failureDetailCode(FailureDetailCodeEnum.D02.getValue())
                .receiverAddress(PhysicalAddressInt.builder()
                        .address(prepareEvent.getReceiverAddress().getAddress())
                        .zip(prepareEvent.getReceiverAddress().getCap())
                        .foreignState(prepareEvent.getReceiverAddress().getCountry())
                        .build())
                .statusDetail("ko")
                .build();

        Mockito.verify(analogWorkflowPaperChannelResponseHandler, Mockito.times(1)).paperChannelPrepareResponseHandler(tmp);
    }

    @Test
    void prepareUpdateTest_KO_fail1() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.KO);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("ko");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        Assertions.assertThrows(PnInternalException.class, ()-> handler.paperChannelResponseReceiver(singleStatusUpdate));

    }

    @Test
    void prepareUpdateTest_KO_fail2() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.KO);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D01);
        prepareEvent.setStatusDetail("ko");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        Assertions.assertThrows(PnInternalException.class, ()-> handler.paperChannelResponseReceiver(singleStatusUpdate));

    }

    @Test
    void prepareUpdateTest_KO_fail3() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.KO);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D02);
        prepareEvent.setStatusDetail("ko");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        Assertions.assertThrows(PnInternalException.class, ()-> handler.paperChannelResponseReceiver(singleStatusUpdate));

    }

    @Test
    void prepareUpdateTest_KO_fail4() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setFailureDetailCode(FailureDetailCodeEnum.D02);
        prepareEvent.setStatusDetail("ko");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        Assertions.assertThrows(PnInternalException.class, ()-> handler.paperChannelResponseReceiver(singleStatusUpdate));

    }

    @Test
    void shouldProcessSendEventWhenPrepareEventIsAbsent() {
        // Arrange
        String requestId = "iun_event_idx_0";
        String iun = "IUN-TEST-SEND-001";

        SendEvent sendEvent = new SendEvent();
        sendEvent.setRequestId(requestId);
        sendEvent.setStatusDescription("OK");

        PaperChannelUpdate update = new PaperChannelUpdate();
        // prepareEvent non impostato → null → entra nell'else if
        update.setSendEvent(sendEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);

        // Act
        handler.paperChannelResponseReceiver(update);

        // Assert — verifica che il processor venga invocato con il normalizer corretto
        Mockito.verify(channelEventProcessor, Mockito.times(1))
                .process(Mockito.any(), Mockito.eq(analogEventNormalizer));
    }


    @Test
    void prepareUpdateTest_PROGRESS() {

        Instant instant = Instant.parse("2022-08-30T16:04:13Z");

        PrepareEvent prepareEvent = new PrepareEvent();
        prepareEvent.setStatusCode(StatusCodeEnum.PROGRESS);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("progress");
        PaperChannelUpdate singleStatusUpdate = new PaperChannelUpdate();
        singleStatusUpdate.setPrepareEvent(prepareEvent);

        Mockito.when(timelineUtils.getIunFromTimelineId("iun_event_idx_0")).thenReturn("iun_event_idx_0");

        handler.paperChannelResponseReceiver(singleStatusUpdate);

        PrepareEventInt tmp = PrepareEventInt.builder()
                .iun("iun_event_idx_0")
                .requestId("iun_event_idx_0")
                .statusCode("PROGRESS")
                .statusDateTime(instant)
                .statusDetail("progress")
                .build();

        Mockito.verify(analogWorkflowPaperChannelResponseHandler, Mockito.times(1)).paperChannelPrepareResponseHandler(tmp);
    }

}