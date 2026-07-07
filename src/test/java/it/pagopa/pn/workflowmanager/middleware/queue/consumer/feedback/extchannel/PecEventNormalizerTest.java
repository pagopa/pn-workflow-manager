package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationInt notification;

    @Mock
    private TimelineElementInternal timelineElementInternal;

    @InjectMocks
    private PecEventNormalizer pecEventNormalizer;

    @Test
    void shouldBuildProgressForC000() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-1")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C000)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-1");
        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(0), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-1"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C000, result.getClassification());
        assertFalse(result.getClassification().isFinalFeedback());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildProgressForC001() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-2")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C001)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-2");
        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(1), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-2"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 1);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C001, result.getClassification());
        assertFalse(result.getClassification().isFinalFeedback());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildProgressForC007() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-3")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C007)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-3");
        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(0), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-3"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C007, result.getClassification());
        assertFalse(result.getClassification().isFinalFeedback());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForC003AsSuccess() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-4")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C003)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-4");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(2), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-4"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.OK), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 2);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C003, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertTrue(result.getClassification().isRecipientReached());
        assertEquals(Optional.of(DesiredFeedbackType.RECEIVED), result.getClassification().getSatisfiedDesiredFeedback());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForC002AsError() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-5")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C002)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-5");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(0), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-5"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C002, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForC004AsError() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-6")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C004)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-6");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(1), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-6"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 1);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C004, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForC006AsError() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-PEC-7")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.C006)
                .build();

        when(notification.getIun()).thenReturn("IUN-PEC-7");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(0), eq(DigitalChannelsInt.PEC), eq("REQ-PEC-7"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = pecEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(PecEventClassification.C006, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }
}

