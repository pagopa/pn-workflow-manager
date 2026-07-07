package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEventCodeInt;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private NotificationInt notification;

    @Mock
    private TimelineElementInternal timelineElementInternal;

    @InjectMocks
    private SmsEventNormalizer smsEventNormalizer;

    @Test
    void shouldBuildFeedbackForS003AsSuccess() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-SMS-1")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.S003)
                .build();

        when(notification.getIun()).thenReturn("IUN-SMS-1");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(0), eq(DigitalChannelsInt.SMS), eq("REQ-SMS-1"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.OK), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = smsEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.SMS, result.getChannel());
        assertEquals(SmsEventClassification.S003, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertEquals(Optional.of(DesiredFeedbackType.SENT), result.getClassification().getSatisfiedDesiredFeedback());
    }

    @Test
    void shouldBuildFeedbackForS008AsError() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-SMS-2")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.S008)
                .build();

        when(notification.getIun()).thenReturn("IUN-SMS-2");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(1), eq(DigitalChannelsInt.SMS), eq("REQ-SMS-2"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = smsEventNormalizer.normalize(event, notification, 1);

        assertNotNull(result);
        assertEquals(ChannelType.SMS, result.getChannel());
        assertEquals(SmsEventClassification.S008, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertEquals(Optional.empty(), result.getClassification().getSatisfiedDesiredFeedback());
    }

    @Test
    void shouldBuildFeedbackForS010AsError() {
        Instant eventTs = Instant.now();
        ExtChannelOutcomeEvent event = ExtChannelOutcomeEvent.builder()
                .requestId("REQ-SMS-3")
                .eventTimestamp(eventTs)
                .eventCode(ExtChannelOutcomeEventCodeInt.S010)
                .build();

        when(notification.getIun()).thenReturn("IUN-SMS-3");
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(0), eq(DigitalChannelsInt.SMS), eq("REQ-SMS-3"),
                any(DigitalDeliveryDetailsInt.class), isNull(), isNull(), eq(ResponseStatusInt.KO), isNull(), eq(eventTs)
        )).thenReturn(timelineElementInternal);

        NormalizedChannelOutcome result = smsEventNormalizer.normalize(event, notification, 0);

        assertNotNull(result);
        assertEquals(ChannelType.SMS, result.getChannel());
        assertEquals(SmsEventClassification.S010, result.getClassification());
        assertTrue(result.getClassification().isFinalFeedback());
        assertEquals(Optional.empty(), result.getClassification().getSatisfiedDesiredFeedback());
    }
}

