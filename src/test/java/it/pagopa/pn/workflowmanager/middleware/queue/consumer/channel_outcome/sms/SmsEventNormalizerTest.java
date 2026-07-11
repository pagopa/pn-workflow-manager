package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

import it.pagopa.pn.commons.log.PnAuditLogEventType;
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
import it.pagopa.pn.workflowmanager.service.AuditLogService;
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
class SmsEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SmsEventNormalizer smsEventNormalizer;

    @Mock
    private NotificationInt notification;
    @Mock
    private TimelineElementInternal mockTimelineElement;
    @Mock
    private ExtChannelOutcomeEventCodeInt eventCode;

    private final String iun = "IUN-TEST-SMS-999";
    private final int recIndex = 0;
    private final String userPhone = "user-phone-01";
    private final SendDigitalMessageDetailsInt sendDigitalMessageDetails = SendDigitalMessageDetailsInt.builder()
            .recIndex(recIndex)
            .digitalAddressSource(null)
            .digitalAddress(InformalDigitalAddressInt.builder()
                    .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.SMS)
                    .address(userPhone)
                    .build()
            )
            .build();
    private final Instant now = Instant.now();
    private final String requestId = "req-sms-123";

    @BeforeEach
    void setUp() {
        lenient().when(notification.getIun()).thenReturn(iun);
    }

    @Test
    void shouldNormalizeSuccessEventWithOkStatus() {
        // Arrange
        String eventCodeValue = "S003";
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent smsEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.SMS), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.OK),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = smsEventNormalizer.normalize(smsEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, SmsEventClassification.S003);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"S008", "S010"})
    void shouldNormalizeNonSuccessEventWithKoStatus(String eventCodeValue) {
        // Arrange
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent smsEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.SMS), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = smsEventNormalizer.normalize(smsEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonAssertions(result, SmsEventClassification.valueOf(eventCodeValue));
    }

    @Test
    void shouldPassCorrectDeliveryDetailCodeAndTimestamp() {
        // Arrange
        String eventCodeValue = "S003";
        when(eventCode.getValue()).thenReturn(eventCodeValue);
        ExtChannelOutcomeEvent smsEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        ArgumentCaptor<DigitalDeliveryDetailsInt> deliveryDetailCaptor = ArgumentCaptor.forClass(DigitalDeliveryDetailsInt.class);

        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.SMS), eq(requestId),
                deliveryDetailCaptor.capture(), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.OK),
                isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        smsEventNormalizer.normalize(smsEvent, notification, sendDigitalMessageDetails);

        // Assert
        DigitalDeliveryDetailsInt capturedDetail = deliveryDetailCaptor.getValue();
        assertEquals(eventCodeValue, capturedDetail.getCode());
        assertEquals(now, capturedDetail.getEventTimestamp());
    }

    @Test
    void shouldThrowExceptionWhenEventCodeIsUnknown() {
        // Arrange
        when(eventCode.getValue()).thenReturn("INVALID_CODE");
        ExtChannelOutcomeEvent smsEvent = ExtChannelOutcomeEvent.builder()
                .eventCode(eventCode)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> smsEventNormalizer.normalize(smsEvent, notification, sendDigitalMessageDetails)
        );

        verifyNoInteractions(timelineUtils);
    }

    private void verifyCommonAssertions(NormalizedChannelOutcome result, SmsEventClassification expectedClassification) {
        assertNotNull(result);
        assertEquals(iun, result.getIun());
        assertEquals(recIndex, result.getRecIndex());
        assertEquals(ChannelType.SMS, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        assertEquals(expectedClassification.name(), result.getOriginalEventType());
        assertEquals(now, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
        verify(auditLogService).buildAuditLogEvent(eq(iun), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_DD_RECEIVE), anyString());

    }
}