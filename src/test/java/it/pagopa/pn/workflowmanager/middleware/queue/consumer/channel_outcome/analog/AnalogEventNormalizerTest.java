package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.SendEventInt;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    private static final String REGISTERED_LETTER_CODE = "rlc-001";
    private static final String STATUS_DETAIL = "CON080";

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
        String progressStatusCode = "PROGRESS";
        SendEventInt sendEvent = SendEventInt.builder()
                .statusCode(progressStatusCode)
                .statusDetail(STATUS_DETAIL)
                .statusDateTime(NOW)
                .prepareRequestId(REQUEST_ID)
                .registeredLetterCode(REGISTERED_LETTER_CODE)
                .build();

        when(timelineUtils.buildSendAnalogProgressNotificationTimelineElement(
                notification, REC_INDEX, sendEvent, sendAnalogDetails
        )).thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = analogEventNormalizer.normalize(sendEvent, notification, sendAnalogDetails);

        // Assert
        verifyCommonAssertions(result, progressStatusCode, AnalogEventClassification.PROGRESS);
        verify(timelineUtils, never()).buildSendAnalogFeedbackNotificationTimelineElement(any(), anyInt(), any(), any(), any());
        verify(auditLogService).buildAuditLogEvent(eq(IUN), eq(REC_INDEX), eq(PnAuditLogEventType.AUD_COM_PD_EXECUTE_RECEIVE), anyString());
    }

    @ParameterizedTest
    @CsvSource({"OK", "KO"})
    void shouldNormalizeFeedbackEvent(String statusCode) {
        // Arrange
        SendEventInt sendEvent = SendEventInt.builder()
                .statusCode(statusCode)
                .statusDetail(STATUS_DETAIL)
                .statusDateTime(NOW)
                .prepareRequestId(REQUEST_ID)
                .registeredLetterCode(REGISTERED_LETTER_CODE)
                .build();

        when(timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                notification, REC_INDEX, sendEvent, sendAnalogDetails, ResponseStatusInt.valueOf(statusCode)
        )).thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = analogEventNormalizer.normalize(sendEvent, notification, sendAnalogDetails);

        // Assert
        verifyCommonAssertions(result, statusCode, AnalogEventClassification.fromStatusEventCode(statusCode));
        verify(timelineUtils, never()).buildSendAnalogProgressNotificationTimelineElement(any(), anyInt(), any(), any());
    }

    private void verifyCommonAssertions(
            NormalizedChannelOutcome result,
            String statusCode,
            AnalogEventClassification expectedClassification
    ) {
        assertNotNull(result);
        assertEquals(IUN, result.getIun());
        assertEquals(REC_INDEX, result.getRecIndex());
        assertEquals(ChannelType.ANALOG, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        String expectedOriginalEventType = statusCode + " - " + STATUS_DETAIL;
        assertEquals(expectedOriginalEventType, result.getOriginalEventType());
        assertEquals(NOW, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }
}