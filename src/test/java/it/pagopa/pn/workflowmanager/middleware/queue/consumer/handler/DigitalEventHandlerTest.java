package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.*;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email.EmailEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec.PecEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms.SmsEventNormalizer;
import org.jetbrains.annotations.NotNull;
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
        DigitalMessageReference reference = new DigitalMessageReference();
        reference.setLocation("https://example.com/message/12345");
        reference.setId("12345");
        reference.setSystem("PEC");
        legal.setGeneratedMessage(reference);

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
        CourtesyMessageProgressEvent courtesy = getCourtesyMessageProgressEvent("REQ-MAIL", CourtesyMessageProgressEvent.EventCodeEnum.M004, "2026-07-07T11:00:00Z");

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
        CourtesyMessageProgressEvent courtesy = getCourtesyMessageProgressEvent("REQ-SMS", CourtesyMessageProgressEvent.EventCodeEnum.S008, "2026-07-07T12:00:00Z");

        SingleStatusUpdate update = new SingleStatusUpdate();
        update.setDigitalCourtesy(courtesy);

        digitalEventHandler.handle(update);

        ArgumentCaptor<ExtChannelOutcomeEvent> captor = ArgumentCaptor.forClass(ExtChannelOutcomeEvent.class);
        verify(channelEventProcessor).process(captor.capture(), eq(smsEventNormalizer));
        assertEquals("REQ-SMS", captor.getValue().getRequestId());
        assertEquals("S008", captor.getValue().getEventCode().getValue());
    }

    private static @NotNull CourtesyMessageProgressEvent getCourtesyMessageProgressEvent(String requestId, CourtesyMessageProgressEvent.EventCodeEnum s008, String text) {
        CourtesyMessageProgressEvent courtesy = new CourtesyMessageProgressEvent();
        courtesy.setRequestId(requestId);
        courtesy.setEventCode(s008);
        courtesy.setStatus(ProgressEventCategory.OK);
        courtesy.setEventTimestamp(Instant.parse(text));
        DigitalMessageReference reference = new DigitalMessageReference();
        reference.setLocation("https://example.com/message/12345");
        reference.setId("12345");
        reference.setSystem("PEC");
        courtesy.setGeneratedMessage(reference);
        return courtesy;
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

}
