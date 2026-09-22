package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.registry.CourtesyChannelSenderRegistry;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.SendCourtesyMessageActionDetails;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.courtesy.CourtesySendOutcome;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.CourtesyChannelFailureReasonInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class SendCourtesyMessageHandlerTest {

    private static final String IUN = "TEST_IUN";
    private static final int REC_INDEX = 0;
    private static final String INTERNAL_ID = "internalId";
    private static final String PA_ID = "paId";

    private static final CommunicationType COMMUNICATION_TYPE = CommunicationType.INFORMAL;
    private static final DeliveryModeInt DELIVERY_MODE = DeliveryModeInt.DIGITAL;
    private static final List<CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT> PLANNED_CHANNELS =
            List.of(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS);

    @Mock
    private InformalCourtesyAddressResolver informalCourtesyAddressResolver;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TimelineService timelineService;
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    @Mock
    private CourtesyChannelSenderRegistry courtesyChannelSenderRegistry;

    private NotificationInt notification;
    @Mock
    private CourtesyAddressSender courtesyAddressSender;
    @Mock
    private TimelineElementInternal timelineElement;

    private PnWorkflowManagerConfigs.CourtesyRetry courtesyRetry;
    private PnWorkflowManagerConfigs.CourtesyRetry.IntervalsMinutes intervalsMinutes;

    private SendCourtesyMessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SendCourtesyMessageHandler(
                informalCourtesyAddressResolver,
                notificationService,
                timelineService,
                timelineUtils,
                schedulerService,
                pnWorkflowManagerConfigs,
                courtesyChannelSenderRegistry
        );

        intervalsMinutes = new PnWorkflowManagerConfigs.CourtesyRetry.IntervalsMinutes();
        courtesyRetry = new PnWorkflowManagerConfigs.CourtesyRetry();
        courtesyRetry.setIntervalsMinutes(intervalsMinutes);

        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(PA_ID)
                .build();
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .internalId(INTERNAL_ID)
                .build();
        notification = NotificationInt.builder()
                .iun(IUN)
                .communicationType(COMMUNICATION_TYPE)
                .recipients(List.of(recipient))
                .sender(sender)
                .build();

        when(notificationService.getInformalNotificationByIun(IUN)).thenReturn(notification);

        lenient().when(pnWorkflowManagerConfigs.getCourtesyRetry()).thenReturn(courtesyRetry);
    }

    // ------------------------------------------------------------------------------------------
    // Indirizzo di cortesia non trovato
    // ------------------------------------------------------------------------------------------

    @Test
    void addressNotFound_resolverReturnsEmptyList_channelClosedWithExpectedFailure() {
        when(informalCourtesyAddressResolver.resolveAddresses(INTERNAL_ID, PA_ID)).thenReturn(List.of());
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.EXPECTED_FAILURE);
        verifyNoInteractions(courtesyChannelSenderRegistry, courtesyAddressSender, schedulerService);
    }

    @Test
    void addressNotFound_onlyOtherChannelsAvailable_channelClosedWithExpectedFailure() {
        when(informalCourtesyAddressResolver.resolveAddresses(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(courtesyAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)));
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.EXPECTED_FAILURE);
        verifyNoInteractions(courtesyChannelSenderRegistry, courtesyAddressSender, schedulerService);
    }

    // ------------------------------------------------------------------------------------------
    // Sender non registrato
    // ------------------------------------------------------------------------------------------

    @Test
    void senderNotRegistered_exceptionIsPropagated_noTimelineNoReschedule() {
        when(informalCourtesyAddressResolver.resolveAddresses(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(courtesyAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)));
        when(courtesyChannelSenderRegistry.getSender(COMMUNICATION_TYPE, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL))
                .thenThrow(new PnInternalException("Nessun sender registrato", "ERROR_CODE"));
        SendCourtesyMessageActionDetails details = details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0);

        assertThrows(PnInternalException.class,
                () -> handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details));

        verifyNoInteractions(courtesyAddressSender, timelineService, schedulerService);
    }

    // ------------------------------------------------------------------------------------------
    // Esito SENT
    // ------------------------------------------------------------------------------------------

    @Test
    void sent_noTimelineNoReschedule() {
        givenSendOutcome(CourtesySendOutcome.SENT);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verify(courtesyChannelSenderRegistry).getSender(COMMUNICATION_TYPE, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);
        verifyNoInteractions(timelineService, timelineUtils, schedulerService);
    }

    // ------------------------------------------------------------------------------------------
    // Esito PERMANENT_FAILURE
    // ------------------------------------------------------------------------------------------

    @Test
    void permanentFailure_channelClosedWithExpectedFailure_noReschedule() {
        givenSendOutcome(CourtesySendOutcome.PERMANENT_FAILURE);
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.EXPECTED_FAILURE);
        verifyNoInteractions(schedulerService);
    }

    // ------------------------------------------------------------------------------------------
    // Esito RETRYABLE_ERROR
    // ------------------------------------------------------------------------------------------

    @Test
    void retryableError_firstAttempt_reschedulesWithFirstIntervalAndIncrementedRetryIndex() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of(5, 10, 15));

        Instant before = Instant.now();

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));
        Instant after = Instant.now();

        SendCourtesyMessageActionDetails nextDetails = verifyRescheduled(before, after, 5);
        assertEquals(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, nextDetails.getChannel());
        assertEquals(1, nextDetails.getRetryIndex());
        verifyNoInteractions(timelineService);
        verify(timelineUtils, never()).buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any());
    }

    @Test
    void retryableError_intermediateAttempt_usesIntervalAtCurrentRetryIndex() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of(5, 10, 15));

        Instant before = Instant.now();
        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 1));
        Instant after = Instant.now();

        SendCourtesyMessageActionDetails nextDetails = verifyRescheduled(before, after, 10);
        assertEquals(2, nextDetails.getRetryIndex());
        verifyNoInteractions(timelineService);
        verify(timelineUtils, never()).buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any());
    }

    @Test
    void retryableError_lastAvailableRetry_stillReschedules() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of(5, 10, 15));

        Instant before = Instant.now();
        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 2));
        Instant after = Instant.now();

        SendCourtesyMessageActionDetails nextDetails = verifyRescheduled(before, after, 15);
        assertEquals(3, nextDetails.getRetryIndex());
        verifyNoInteractions(timelineService);
        verify(timelineUtils, never()).buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any());
    }

    @Test
    void retryableError_intervalsExhausted_channelClosedWithRetriesExhausted() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of(5, 10, 15));
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 3));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
    }

    @Test
    void retryableError_emptyIntervalsForChannel_channelClosedWithRetriesExhausted() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of());
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
    }

    @Test
    void retryableError_nullIntervalsForChannel_channelClosedWithRetriesExhausted() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(null);
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
    }

    @Test
    void retryableError_nullIntervalsMinutesConfig_channelClosedWithRetriesExhausted() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);
        courtesyRetry.setIntervalsMinutes(null);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
    }

    @Test
    void retryableError_nullCourtesyRetryConfig_channelClosedWithRetriesExhausted() {
        givenSendOutcome(CourtesySendOutcome.RETRYABLE_ERROR);
        when(timelineUtils.buildCourtesyChannelFailedTimelineElement(any(), any(), any(), any(), any(), any()))
                .thenReturn(timelineElement);
        when(pnWorkflowManagerConfigs.getCourtesyRetry()).thenReturn(null);

        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, 0));

        verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, CourtesyChannelFailureReasonInt.RETRIES_EXHAUSTED);
    }

    /**
     * Verifica che per ogni canale vengano usati gli intervalli di retry del canale corretto
     * (EMAIL -> email, SMS -> sms, APPIO -> io, TPP -> tpp).
     */
    @ParameterizedTest
    @EnumSource(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.class)
    void retryableError_usesIntervalsOfTheRelatedChannel(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel) {
        when(courtesyChannelSenderRegistry.getSender(COMMUNICATION_TYPE, channel)).thenReturn(courtesyAddressSender);
        CourtesyDigitalAddressInt courtesyAddress = courtesyAddress(channel);
        when(informalCourtesyAddressResolver.resolveAddresses(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(courtesyAddress));
        when(courtesyAddressSender.send(notification, courtesyAddress, REC_INDEX)).thenReturn(CourtesySendOutcome.RETRYABLE_ERROR);
        intervalsMinutes.setEmail(List.of(1));
        intervalsMinutes.setSms(List.of(2));
        intervalsMinutes.setIo(List.of(3));
        intervalsMinutes.setTpp(List.of(4));

        int expectedWaitMinutes = switch (channel) {
            case EMAIL -> 1;
            case SMS -> 2;
            case APPIO -> 3;
            case TPP -> 4;
        };

        Instant before = Instant.now();
        handler.handleSendCourtesyMessageAction(IUN, REC_INDEX, details(channel, 0));
        Instant after = Instant.now();

        SendCourtesyMessageActionDetails nextDetails = verifyRescheduled(before, after, expectedWaitMinutes);
        assertEquals(channel, nextDetails.getChannel());
        assertEquals(1, nextDetails.getRetryIndex());
    }

    // ------------------------------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------------------------------

    private SendCourtesyMessageActionDetails details(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel, int retryIndex) {
        return SendCourtesyMessageActionDetails.builder()
                .channel(channel)
                .retryIndex(retryIndex)
                .deliveryMode(DELIVERY_MODE)
                .plannedChannels(PLANNED_CHANNELS)
                .build();
    }

    private CourtesyDigitalAddressInt courtesyAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT type) {
        return CourtesyDigitalAddressInt.builder()
                .type(type)
                .address("test")
                .build();
    }

    private void givenSendOutcome(CourtesySendOutcome outcome) {
        CourtesyDigitalAddressInt courtesyAddress = courtesyAddress(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL);
        when(informalCourtesyAddressResolver.resolveAddresses(INTERNAL_ID, PA_ID))
                .thenReturn(List.of(courtesyAddress));
        when(courtesyChannelSenderRegistry.getSender(COMMUNICATION_TYPE, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)).thenReturn(courtesyAddressSender);
        when(courtesyAddressSender.send(notification, courtesyAddress, REC_INDEX)).thenReturn(outcome);
    }

    /** Verifica che il canale sia stato chiuso con il motivo atteso e che non sia stato riprogrammato nulla. */
    private void verifyChannelFailed(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel, CourtesyChannelFailureReasonInt expectedReason) {
        String expectedEventId = TimelineEventId.COURTESY_CHANNEL_FAILED.buildEventId(EventId.builder()
                .iun(IUN)
                .recIndex(REC_INDEX)
                .courtesyAddressType(channel)
                .build());

        verify(timelineUtils).buildCourtesyChannelFailedTimelineElement(
                eq(REC_INDEX), eq(notification), eq(channel), eq(DELIVERY_MODE), eq(expectedReason), eq(expectedEventId));
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verify(schedulerService, never()).scheduleEvent(any(), any(), any(), any(), any());
    }

    /**
     * Verifica che l'action sia stata riprogrammata con la data attesa (now + waitMinutes), che deliveryMode e
     * plannedChannels siano stati propagati, e restituisce i details usati per la nuova schedulazione.
     */
    private SendCourtesyMessageActionDetails verifyRescheduled(Instant before, Instant after, int waitMinutes) {
        ArgumentCaptor<Instant> dateCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<SendCourtesyMessageActionDetails> detailsCaptor = ArgumentCaptor.forClass(SendCourtesyMessageActionDetails.class);

        verify(schedulerService).scheduleEvent(
                eq(IUN),
                eq(REC_INDEX),
                dateCaptor.capture(),
                eq(ActionType.SEND_COURTESY_MESSAGE_ACTION),
                detailsCaptor.capture());

        Instant scheduledDate = dateCaptor.getValue();
        Duration wait = Duration.ofMinutes(waitMinutes);
        assertFalse(scheduledDate.isBefore(before.plus(wait)), "La data di schedulazione e' antecedente al minimo atteso");
        assertFalse(scheduledDate.isAfter(after.plus(wait)), "La data di schedulazione e' successiva al massimo atteso");

        SendCourtesyMessageActionDetails nextDetails = detailsCaptor.getValue();
        assertEquals(DELIVERY_MODE, nextDetails.getDeliveryMode());
        assertEquals(PLANNED_CHANNELS, nextDetails.getPlannedChannels());

        verify(timelineService, never()).addTimelineElement(any(), any());
        return nextDetails;
    }
}