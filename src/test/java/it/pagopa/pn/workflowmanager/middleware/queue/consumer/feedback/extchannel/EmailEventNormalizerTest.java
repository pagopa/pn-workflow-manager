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
class EmailEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationInt notification;

    @Mock
    private TimelineElementInternal timelineElementInternal;

    @InjectMocks
    private EmailEventNormalizer emailEventNormalizer;

    @Test
    void shouldBuildProgressForM003() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-EMAIL-1")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.M003)
                .build();

        when(notification.getIun()).thenReturn("IUN-EMAIL-1");
        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(0), eq(DigitalChannelsInt.EMAIL), eq("REQ-EMAIL-1"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = emailEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(EmailEventClassification.M003, result.getClassification());
        assertEquals(Optional.of(DesiredFeedbackType.SENT), result.getClassification().getSatisfiedDesiredFeedback());
        assertFalse(result.getClassification().isFinalFeedback());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildProgressForM004() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-EMAIL-3")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.M004)
                .build();

        when(notification.getIun()).thenReturn("IUN-EMAIL-3");
        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(2), eq(DigitalChannelsInt.EMAIL), eq("REQ-EMAIL-3"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = emailEventNormalizer.normalize(event, notification, 2);

        assertNotNull(result);
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(EmailEventClassification.M004, result.getClassification());
        assertFalse(result.getClassification().isFinalFeedback());
        assertTrue(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForM005() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-EMAIL-2")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.M005)
                .build();

        when(notification.getIun()).thenReturn("IUN-EMAIL-2");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(1), eq(DigitalChannelsInt.EMAIL), eq("REQ-EMAIL-2"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = emailEventNormalizer.normalize(event, notification, 1);

        assertNotNull(result);
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(EmailEventClassification.M005, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForM006() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-EMAIL-4")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.M006)
                .build();

        when(notification.getIun()).thenReturn("IUN-EMAIL-4");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(0), eq(DigitalChannelsInt.EMAIL), eq("REQ-EMAIL-4"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = emailEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(EmailEventClassification.M006, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldBuildFeedbackForM009() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-EMAIL-5")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.M009)
                .build();

        when(notification.getIun()).thenReturn("IUN-EMAIL-5");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(1), eq(DigitalChannelsInt.EMAIL), eq("REQ-EMAIL-5"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = emailEventNormalizer.normalize(event, notification, 1);

        assertNotNull(result);
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(EmailEventClassification.M009, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertFalse(result.getClassification().isRecipientReached());
        assertEquals(timelineElementInternal, result.getTimelineElementInternal());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }
}
