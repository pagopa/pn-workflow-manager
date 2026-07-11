package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEventCodeInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private EmailEventNormalizer emailEventNormalizer;

    @Mock
    private NotificationInt notification;
    @Mock
    private TimelineElementInternal mockTimelineElement;
    @Mock
    private ExtChannelOutcomeEventCodeInt eventCode;

    private final String iun = "IUN-TEST-EMAIL-999";
    private final int recIndex = 0;
    private final String userEmail = "user-email-01";
    private final SendDigitalMessageDetailsInt sendDigitalMessageDetails = SendDigitalMessageDetailsInt.builder()
            .recIndex(recIndex)
            .digitalAddressSource(null)
            .digitalAddress(InformalDigitalAddressInt.builder()
                    .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL)
                    .address(userEmail)
                    .build()
            )
            .build();
    private final Instant now = Instant.now();
    private final String requestId = "req-email-123";

    @BeforeEach
    void setUp() {
        lenient().when(notification.getIun()).thenReturn(iun);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"M003", "M004"})
    void shouldNormalizeProgressEvent(String eventCodeValue) {
        // Arrange
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent emailEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.EMAIL), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = emailEventNormalizer.normalize(emailEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, EmailEventClassification.valueOf(eventCodeValue));
        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(
                any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"M005", "M006", "M008", "M009", "M010", "M011"})
    void shouldNormalizeFeedbackEventAlwaysWithKoStatusAndNullReceipts(String eventCodeValue) {
        // Arrange
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent emailEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.EMAIL), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = emailEventNormalizer.normalize(emailEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, EmailEventClassification.valueOf(eventCodeValue));
        verify(timelineUtils, never()).buildSendDigitalMessageProgress(
                any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldPassCorrectDeliveryDetailCodeAndTimestamp() {
        // Arrange
        String eventCodeValue = "M003";
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent emailEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        ArgumentCaptor<DigitalDeliveryDetailsInt> deliveryDetailCaptor = ArgumentCaptor.forClass(DigitalDeliveryDetailsInt.class);

        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.EMAIL), eq(requestId),
                deliveryDetailCaptor.capture(), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        emailEventNormalizer.normalize(emailEvent, notification, sendDigitalMessageDetails);

        // Assert
        DigitalDeliveryDetailsInt capturedDetail = deliveryDetailCaptor.getValue();
        assertEquals(eventCodeValue, capturedDetail.getCode());
        assertEquals(now, capturedDetail.getEventTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenEventCodeIsUnknown() {
        // Arrange
        when(eventCode.getValue()).thenReturn("INVALID_CODE");
        ExtChannelOutcomeEvent emailEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> emailEventNormalizer.normalize(emailEvent, notification, sendDigitalMessageDetails)
        );

        verifyNoInteractions(timelineUtils);
    }

    private void verifyCommonAssertions(NormalizedChannelOutcome result, EmailEventClassification expectedClassification) {
        assertNotNull(result);
        assertEquals(iun, result.getIun());
        assertEquals(recIndex, result.getRecIndex());
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        assertEquals(expectedClassification.name(), result.getOriginalEventType());
        assertEquals(now, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }
}