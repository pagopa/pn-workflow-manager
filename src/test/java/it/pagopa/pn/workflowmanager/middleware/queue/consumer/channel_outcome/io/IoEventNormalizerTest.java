package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.event.NotificationViewedInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEventType;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.utils.NotificationPaymentUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IoEventNormalizerTest {

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private IoEventNormalizer ioEventNormalizer;

    // Mock e stub di supporto comuni
    @Mock
    private NotificationInt notification;
    @Mock
    private NotificationSenderInt sender; // Assumi sia il tipo corretto per getSender()
    @Mock
    private TimelineElementInternal mockTimelineElement;

    private MockedStatic<NotificationPaymentUtils> paymentUtilsMockedStatic;

    private final String iun = "IUN-TEST-999";
    private final int recIndex = 0;
    private final String userTaxId = "tax-user-01";
    private final SendDigitalMessageDetailsInt sendDigitalMessageDetails = SendDigitalMessageDetailsInt.builder()
            .recIndex(recIndex)
            .digitalAddressSource(null)
            .digitalAddress(InformalDigitalAddressInt.builder()
                    .type(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.APPIO)
                    .address(userTaxId)
                    .build()
            )
            .build();
    private final Instant now = Instant.now();
    private final String requestId = "req-123";
    private final String paTaxId = "tax-pa-01";
    private final String noticeCode = "notice-xyz";

    @BeforeEach
    void setUp() {
        lenient().when(notification.getIun()).thenReturn(iun);
        lenient().when(notification.getSender()).thenReturn(sender);
        lenient().when(sender.getPaTaxId()).thenReturn(paTaxId);

        // Apriamo il mock statico prima di ogni test
        paymentUtilsMockedStatic = mockStatic(NotificationPaymentUtils.class);
    }

    @AfterEach
    void tearDown() {
        // Chiudiamo il mock statico per evitare leak tra i test
        paymentUtilsMockedStatic.close();
    }

    @Test
    void shouldNormalizeSentToIoEvent() {
        // Arrange
        IoOutcomeEvent ioEvent = IoOutcomeEvent.builder()
                .eventType(IoOutcomeEventType.SENT_TO_IO)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.IO), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = ioEventNormalizer.normalize(ioEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonsAssertionsForNormalizedChannelOutcome(result, IoEventClassification.SENT_TO_IO);
        assertTrue(result.getTriggers().isEmpty());
    }

    @Test
    void shouldNormalizeDeliveredToUserEvent() {
        // Arrange
        IoOutcomeEvent ioEvent = IoOutcomeEvent.builder()
                .eventType(IoOutcomeEventType.DELIVERED_TO_USER)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageProgress(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.IO), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = ioEventNormalizer.normalize(ioEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonsAssertionsForNormalizedChannelOutcome(result, IoEventClassification.DELIVERED_TO_USER);
        assertTrue(result.getTriggers().isEmpty());
    }

    @Test
    void shouldIncludeViewedTriggerWhenEventIsRead() {
        // Arrange
        IoOutcomeEvent ioEvent = IoOutcomeEvent.builder()
                .eventType(IoOutcomeEventType.READ)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        when(timelineUtils.buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = ioEventNormalizer.normalize(ioEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonsAssertionsForNormalizedChannelOutcome(result, IoEventClassification.READ);
        Set<ChannelEventTrigger> triggers = result.getTriggers();
        assertEquals(1, triggers.size());

        ChannelEventTrigger trigger = triggers.iterator().next();
        assertInstanceOf(NotificationViewedInt.class, trigger);

        NotificationViewedInt viewedTrigger = (NotificationViewedInt) trigger;
        assertEquals(iun, viewedTrigger.getIun());
        assertEquals(recIndex, viewedTrigger.getRecipientIndex());
        assertEquals(ChannelType.IO.name(), viewedTrigger.getSourceChannel());
        assertEquals(now, viewedTrigger.getViewedDate());

        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }

    @Test
    void shouldIncludePaidTriggerWithAmountWhenEventIsPaid() {
        // Arrange
        IoOutcomeEvent ioEvent = IoOutcomeEvent.builder()
                .eventType(IoOutcomeEventType.PAID)
                .eventTimestamp(now)
                .requestId(requestId)
                .noticeCode(noticeCode)
                .build();

        int expectedAmount = 15000;
        paymentUtilsMockedStatic.when(() -> NotificationPaymentUtils.getAmountFromNotificationPagoPaPayment(notification, recIndex, noticeCode))
                .thenReturn(expectedAmount);

        when(timelineUtils.buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = ioEventNormalizer.normalize(ioEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonsAssertionsForNormalizedChannelOutcome(result, IoEventClassification.PAID);
        Set<ChannelEventTrigger> triggers = result.getTriggers();
        assertEquals(1, triggers.size());

        ChannelEventTrigger trigger = triggers.iterator().next();
        assertInstanceOf(NotificationPaidInt.class, trigger);

        NotificationPaidInt paidTrigger = (NotificationPaidInt) trigger;
        assertEquals(iun, paidTrigger.getIun());
        assertEquals(noticeCode, paidTrigger.getNoticeCode());
        assertEquals(paTaxId, paidTrigger.getCreditorTaxId());
        assertEquals(now, paidTrigger.getEventTimestamp());
        assertEquals(ChannelType.IO.name(), paidTrigger.getPaymentSourceChannel());
        assertEquals(expectedAmount, paidTrigger.getAmount());
    }

    @Test
    void shouldInvokeFeedbackWithKoWhenSenderNotAllowed() {
        // Arrange
        IoOutcomeEvent ioEvent = IoOutcomeEvent.builder()
                .eventType(IoOutcomeEventType.SENDER_NOT_ALLOWED)
                .eventTimestamp(now)
                .requestId(requestId)
                .build();

        // Ci aspettiamo il metodo di Feedback anziché Progress
        when(timelineUtils.buildSendDigitalMessageFeedback(
                eq(notification), eq(recIndex), eq(DigitalChannelsInt.IO), eq(requestId),
                any(DigitalDeliveryDetailsInt.class), eq(sendDigitalMessageDetails.getDigitalAddress()),
                eq(sendDigitalMessageDetails.getDigitalAddressSource()), eq(ResponseStatusInt.KO), isNull(), eq(now)))
                .thenReturn(mockTimelineElement);

        // Act
        NormalizedChannelOutcome result = ioEventNormalizer.normalize(ioEvent, notification, sendDigitalMessageDetails);

        // Assert
        verifyCommonsAssertionsForNormalizedChannelOutcome(result, IoEventClassification.SENDER_NOT_ALLOWED);
        assertTrue(result.getTriggers().isEmpty());

        verify(timelineUtils, never()).buildSendDigitalMessageProgress(any(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    private void verifyCommonsAssertionsForNormalizedChannelOutcome(NormalizedChannelOutcome result, IoEventClassification expectedClassification) {
        assertNotNull(result);
        assertEquals(iun, result.getIun());
        assertEquals(ChannelType.IO, result.getChannel());
        assertEquals(expectedClassification, result.getClassification());
        assertEquals(expectedClassification.name(), result.getOriginalEventType());
        assertEquals(now, result.getEventTimestamp());
        assertEquals(mockTimelineElement, result.getTimelineElementInternal());
    }
}