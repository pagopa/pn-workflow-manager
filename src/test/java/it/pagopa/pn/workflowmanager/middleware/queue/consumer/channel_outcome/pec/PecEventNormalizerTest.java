package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
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
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.DigitalMessageReferenceInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEventCodeInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
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
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private PecEventNormalizer pecEventNormalizer;

    @Mock
    private NotificationInt notification;
    @Mock
    private TimelineElementInternal mockTimelineElement;
    @Mock
    private ExtChannelOutcomeEventCodeInt eventCode;
    @Mock
    private DigitalMessageReferenceInt generatedMessage;

    private final String iun = "IUN-TEST-PEC-999";
    private final int recIndex = 0;
    private final String userPec = "user-pec-01";
    private final SendDigitalMessageDetailsInt sendDigitalMessageDetails = SendDigitalMessageDetailsInt.builder()
            .recIndex(recIndex)
            .digitalAddressSource(null)
            .digitalAddress(InformalDigitalAddressInt.builder()
                    .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC)
                    .address(userPec)
                    .build()
            )
            .build();
    private final Instant now = Instant.now();
    private final String requestId = "req-pec-123";

    @BeforeEach
    void setUp() {
        lenient().when(notification.getIun()).thenReturn(iun);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {
            "C000", "C001", "C005", "C007"
    })
    void shouldNormalizeProgressEvent(String eventCodeValue) {
        // Arrange
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.PEC), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, PecEventClassification.fromEventCode(eventCodeValue));
        verify(timelineUtils, never()).buildSendDigitalMessageFeedback(
                any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {
            "C002", "C004", "C006", "C008", "C009", "C010", "C011"
    })
    void shouldNormalizeFeedbackEventWithKoStatusWhenNotSuccessfulDelivery(String eventCodeValue) {
        // Arrange
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.PEC), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, PecEventClassification.fromEventCode(eventCodeValue));
        verify(timelineUtils, never()).buildSendDigitalMessageProgress(
                any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldNormalizeSuccessfulDeliveryEventWithOkStatusAndReceipts() {
        // Arrange
        when(eventCode.getValue()).thenReturn("C003");
        when(generatedMessage.getId()).thenReturn("msg-id-001");
        when(generatedMessage.getSystem()).thenReturn("system-x");

        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .generatedMessage(generatedMessage)
                .build();

        ArgumentCaptor<List<SendingReceipt>> receiptsCaptor = ArgumentCaptor.forClass(List.class);

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.PEC), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.OK),
                receiptsCaptor.capture(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, PecEventClassification.C003);

        List<SendingReceipt> capturedReceipts = receiptsCaptor.getValue();
        assertNotNull(capturedReceipts);
        assertEquals(1, capturedReceipts.size());
        assertEquals("msg-id-001", capturedReceipts.getFirst().getId());
        assertEquals("system-x", capturedReceipts.getFirst().getSystem());
    }

    @Test
    void shouldNormalizeFeedbackEventWithNullReceiptsWhenGeneratedMessageIsNull() {
        // Arrange
        when(eventCode.getValue()).thenReturn("C004");
        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .generatedMessage(null)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.PEC), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, PecEventClassification.C004);
    }

    @Test
    void shouldNormalizeFeedbackEventWithNullReceiptsWhenGeneratedMessageIdIsNull() {
        // Arrange
        when(eventCode.getValue()).thenReturn("C006");
        when(generatedMessage.getId()).thenReturn(null);
        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .generatedMessage(generatedMessage)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.PEC), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, PecEventClassification.C006);
    }

    @Test
    void shouldThrowExceptionWhenEventCodeIsUnknown() {
        // Arrange
        when(eventCode.getValue()).thenReturn("INVALID_CODE");
        ExtChannelOutcomeEvent pecEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> pecEventNormalizer.normalize(pecEvent, notification, sendDigitalMessageDetails)
        );

        verifyNoInteractions(timelineUtils);
    }

    private void verifyCommonAssertions(NormalizedChannelOutcome result, PecEventClassification expectedClassification) {
        assertNotNull(result);
        assertEquals(iun, result.getIun());
        assertEquals(recIndex, result.getRecIndex());
        assertEquals(ChannelType.PEC, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        assertEquals(expectedClassification.name(), result.getOriginalEventType());
        assertEquals(now, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }
}

