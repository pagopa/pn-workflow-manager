package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.CourtesyMessageProgressEvent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.LegalMessageSentDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.ProgressEventCategory;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.EmailEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.PecEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.SmsEventNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DigitalEventHandlerTest {

    @Mock
    private ChannelEventProcessor channelEventProcessor;
    @Mock
    private PecEventNormalizer pecEventNormalizer;
    @Mock
    private EmailEventNormalizer emailEventNormalizer;
    @Mock
    private SmsEventNormalizer smsEventNormalizer;

    @InjectMocks
    private DigitalEventHandler digitalEventHandler;

    @Test
    void shouldRouteLegalEventToPecNormalizer() {
        LegalMessageSentDetails legal = new LegalMessageSentDetails();
        legal.setRequestId("REQ-PEC");
        legal.setEventCode(LegalMessageSentDetails.EventCodeEnum.C003);
        legal.setStatus(ProgressEventCategory.OK);
        legal.setEventTimestamp(Instant.parse("2026-07-07T10:00:00Z"));

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalLegal(legal);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(pecEventNormalizer));
        assertEquals("REQ-PEC", captor.getValue().getRequestId());
        assertEquals("C003", captor.getValue().getEventCode().getValue());
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), eq(emailEventNormalizer));
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), eq(smsEventNormalizer));
    }

    @Test
    void shouldRouteCourtesyMailEventToEmailNormalizer() {
        CourtesyMessageProgressEvent courtesy = new CourtesyMessageProgressEvent();
        courtesy.setRequestId("REQ-MAIL");
        courtesy.setEventCode(CourtesyMessageProgressEvent.EventCodeEnum.M004);
        courtesy.setStatus(ProgressEventCategory.OK);
        courtesy.setEventTimestamp(Instant.parse("2026-07-07T11:00:00Z"));

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalCourtesy(courtesy);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(emailEventNormalizer));
        assertEquals("REQ-MAIL", captor.getValue().getRequestId());
        assertEquals("M004", captor.getValue().getEventCode().getValue());
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), eq(smsEventNormalizer));
    }

    @Test
    void shouldRouteCourtesySmsEventToSmsNormalizer() {
        CourtesyMessageProgressEvent courtesy = new CourtesyMessageProgressEvent();
        courtesy.setRequestId("REQ-SMS");
        courtesy.setEventCode(CourtesyMessageProgressEvent.EventCodeEnum.S008);
        courtesy.setStatus(ProgressEventCategory.OK);
        courtesy.setEventTimestamp(Instant.parse("2026-07-07T12:00:00Z"));

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalCourtesy(courtesy);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(smsEventNormalizer));
        assertEquals("REQ-SMS", captor.getValue().getRequestId());
        assertEquals("S008", captor.getValue().getEventCode().getValue());
    }

    @Test
    void shouldThrowInternalExceptionWhenNoDigitalPayloadPresent() {
        SingleStatusUpdate update = new SingleStatusUpdate();

        assertThrows(PnInternalException.class, () -> digitalEventHandler.handle(update));

        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), any(PecEventNormalizer.class));
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), any(EmailEventNormalizer.class));
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), any(SmsEventNormalizer.class));
    }

    @Test
    void shouldPreferLegalBranchWhenBothLegalAndCourtesyArePresent() {
        LegalMessageSentDetails legal = new LegalMessageSentDetails();
        legal.setRequestId("REQ-LEGAL-FIRST");
        legal.setEventCode(LegalMessageSentDetails.EventCodeEnum.C001);
        legal.setStatus(ProgressEventCategory.OK);
        legal.setEventTimestamp(Instant.parse("2026-07-07T13:00:00Z"));

        CourtesyMessageProgressEvent courtesy = new CourtesyMessageProgressEvent();
        courtesy.setRequestId("REQ-SMS-SHOULD-NOT-BE-USED");
        courtesy.setEventCode(CourtesyMessageProgressEvent.EventCodeEnum.S010);
        courtesy.setStatus(ProgressEventCategory.OK);
        courtesy.setEventTimestamp(Instant.parse("2026-07-07T14:00:00Z"));

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalLegal(legal);
        update.setDigitalCourtesy(courtesy);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(pecEventNormalizer));
        assertEquals("REQ-LEGAL-FIRST", captor.getValue().getRequestId());
        assertEquals("C001", captor.getValue().getEventCode().getValue());
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), eq(emailEventNormalizer));
        verify(channelEventProcessor, never()).process(any(ExtChannelOutcomeEvent.class), eq(smsEventNormalizer));
    }

    @Test
    void shouldUseEnvelopeTimestampWhenCourtesyTimestampIsNull() {
        Instant envelopeTs = Instant.parse("2026-07-07T15:00:00Z");

        CourtesyMessageProgressEvent courtesy = new CourtesyMessageProgressEvent();
        courtesy.setRequestId("REQ-MAIL-NO-TS");
        courtesy.setEventCode(CourtesyMessageProgressEvent.EventCodeEnum.M003);
        courtesy.setStatus(ProgressEventCategory.OK);
        courtesy.setEventTimestamp(null);

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalCourtesy(courtesy);
        update.setEventTimestamp(envelopeTs);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(emailEventNormalizer));
        assertEquals(envelopeTs, captor.getValue().getEventTimestamp());
        assertEquals("M003", captor.getValue().getEventCode().getValue());
    }

}
