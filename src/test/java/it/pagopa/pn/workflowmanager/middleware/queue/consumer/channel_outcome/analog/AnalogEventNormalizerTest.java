package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.SendEventInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalogEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AnalogEventNormalizer analogEventNormalizer;

    @Mock
    private NotificationInt notification;
    @Mock
    private TimelineElementInternal mockTimelineElement;

    private static final String IUN = "IUN-TEST-ANALOG-001";
    private static final int REC_INDEX = 0;
    private static final Instant NOW = Instant.now();
    private static final String REQUEST_ID = "req-analog-123";
    private static final String STATUS_CODE = "CON080";
    private static final String REGISTERED_LETTER_CODE = "rlc-001";
    private static final Integer FIRST_ATTEMPT = 0;

    private final SendAnalogMessageDetailsInt sendAnalogDetails = SendAnalogMessageDetailsInt.builder()
            .recIndex(REC_INDEX)
            .build();

    @BeforeEach
    void setUp() {
        lenient().when(notification.getIun()).thenReturn(IUN);
        lenient().when(notification.getSentAt()).thenReturn(NOW);
    }

    @Test
    void shouldNormalizeProgressEvent() {
        // Arrange
        SendEventInt sendEvent = SendEventInt.builder()
                .statusDescription("PROGRESS")
                .statusCode(STATUS_CODE)
                .statusDateTime(NOW)
                .requestId(REQUEST_ID)
                .registeredLetterCode(REGISTERED_LETTER_CODE)
                .build();

        when(timelineUtils.buildSendAnalogProgressNotificationTimelineElement(
                eq(REC_INDEX), eq(notification), isNull(), eq(NOW),
                any(AnalogDeliveryDetailsInt.class), eq(AnalogDeliveryTypeInt.RS),
                isNull(), eq(REQUEST_ID), eq(REGISTERED_LETTER_CODE), eq(FIRST_ATTEMPT)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = analogEventNormalizer.normalize(sendEvent, notification, sendAnalogDetails);

        // Assert
        verifyCommonAssertions(result, AnalogEventClassification.PROGRESS);
        verify(timelineUtils, never()).buildSendAnalogFeedbackNotificationTimelineElement(
                anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt());
        verify(auditLogService).buildAuditLogEvent(eq(IUN), eq(REC_INDEX), eq(PnAuditLogEventType.AUD_COM_PD_EXECUTE_RECEIVE), anyString());
    }

    @Test
    void shouldNormalizeFeedbackEventWithOkStatus() {
        // Arrange
        SendEventInt sendEvent = SendEventInt.builder()
                .statusDescription("OK")
                .statusCode(STATUS_CODE)
                .statusDateTime(NOW)
                .requestId(REQUEST_ID)
                .registeredLetterCode(REGISTERED_LETTER_CODE)
                .build();

        when(timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                eq(REC_INDEX), eq(notification), isNull(), eq(NOW),
                any(AnalogDeliveryDetailsInt.class), eq(AnalogDeliveryTypeInt.RS),
                isNull(), eq(REQUEST_ID), eq(REGISTERED_LETTER_CODE),
                isNull(), eq(ResponseStatusInt.OK), isNull(), eq(FIRST_ATTEMPT)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = analogEventNormalizer.normalize(sendEvent, notification, sendAnalogDetails);

        // Assert
        verifyCommonAssertions(result, AnalogEventClassification.OK);
        verify(timelineUtils, never()).buildSendAnalogProgressNotificationTimelineElement(
                anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    void shouldNormalizeFeedbackEventWithKoStatus() {
        // Arrange
        SendEventInt sendEvent = SendEventInt.builder()
                .statusDescription("KO")
                .statusCode(STATUS_CODE)
                .statusDateTime(NOW)
                .requestId(REQUEST_ID)
                .registeredLetterCode(REGISTERED_LETTER_CODE)
                .build();

        when(timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                eq(REC_INDEX), eq(notification), isNull(), eq(NOW),
                any(AnalogDeliveryDetailsInt.class), eq(AnalogDeliveryTypeInt.RS),
                isNull(), eq(REQUEST_ID), eq(REGISTERED_LETTER_CODE),
                isNull(), eq(ResponseStatusInt.KO), isNull(), eq(FIRST_ATTEMPT)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = analogEventNormalizer.normalize(sendEvent, notification, sendAnalogDetails);

        // Assert
        verifyCommonAssertions(result, AnalogEventClassification.KO);
        verify(timelineUtils, never()).buildSendAnalogProgressNotificationTimelineElement(
                anyInt(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt());
    }

    private void verifyCommonAssertions(NormalizedChannelOutcome result, AnalogEventClassification expectedClassification) {
        assertNotNull(result);
        assertEquals(IUN, result.getIun());
        assertEquals(REC_INDEX, result.getRecIndex());
        assertEquals(ChannelType.ANALOG, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        assertEquals(expectedClassification.name(), result.getOriginalEventType());
        assertEquals(NOW, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }
}