package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.SendCourtesyMessageActionDetails;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtesyAddressActionDispatcherTest {
    @Mock
    private InformalCourtesyAddressResolver informalCourtesyAddressResolver;

    @Mock
    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private CourtesyAddressActionDispatcher dispatcher;

    private NotificationInt notification;

    @BeforeEach
    void setUp() {
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .internalId("recipientId")
                .build();

        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId("senderId")
                .build();

        notification = NotificationInt.builder()
                .iun("IUN-123")
                .sender(sender)
                .recipients(List.of(recipient))
                .build();
    }

    @Test
    void dispatchSchedulesEventForEachResolvedAddress() {

        CourtesyDigitalAddressInt emailAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("test@test.it")
                .build();

        when(informalCourtesyAddressResolver.resolveAddresses("recipientId", "senderId"))
                .thenReturn(List.of(emailAddress));

        dispatcher.dispatch(notification, 0);

        verify(schedulerService, times(1)).scheduleEvent(
                eq("IUN-123"), eq(0), any(Instant.class), eq(ActionType.SEND_COURTESY_MESSAGE_ACTION), any(SendCourtesyMessageActionDetails.class));
    }

    @Test
    void dispatchSkipsSmsWhenSmsCourtesyIsDisabled() {

        CourtesyDigitalAddressInt emailAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("test@test.it")
                .build();
        CourtesyDigitalAddressInt smsAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address("1234567890")
                .build();

        when(informalCourtesyAddressResolver.resolveAddresses("recipientId", "senderId"))
                .thenReturn(List.of(emailAddress, smsAddress));
        when(pnWorkflowManagerConfigs.getSmsCourtesyEnabled()).thenReturn(false);

        dispatcher.dispatch(notification, 0);

        verify(schedulerService, times(1)).scheduleEvent(
                eq("IUN-123"), eq(0), any(Instant.class), eq(ActionType.SEND_COURTESY_MESSAGE_ACTION), any(SendCourtesyMessageActionDetails.class));

    }

    @Test
    void dispatchSchedulesSmsWhenSmsCourtesyIsEnabled() {

        CourtesyDigitalAddressInt emailAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("test@test.it")
                .build();
        CourtesyDigitalAddressInt smsAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address("1234567890")
                .build();

        when(informalCourtesyAddressResolver.resolveAddresses("recipientId", "senderId"))
                .thenReturn(List.of(emailAddress, smsAddress));
        when(pnWorkflowManagerConfigs.getSmsCourtesyEnabled()).thenReturn(true);

        dispatcher.dispatch(notification, 0);

        verify(schedulerService, times(2)).scheduleEvent(
                eq("IUN-123"), eq(0), any(Instant.class), eq(ActionType.SEND_COURTESY_MESSAGE_ACTION), any(SendCourtesyMessageActionDetails.class));
    }

    @Test
    void dispatchDoesNothingWhenNoCourtesyAddressesResolved() {

        when(informalCourtesyAddressResolver.resolveAddresses("recipientId", "senderId"))
                .thenReturn(List.of());

        dispatcher.dispatch(notification, 0);

        verify(schedulerService, never()).scheduleEvent(any(), any(), any(), any(), any());
    }

    @Test
    void dispatchDoesNothingWhenEmailCourtesyAddressIsMissing() {

        CourtesyDigitalAddressInt smsAddress = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.SMS)
                .address("1234567890")
                .build();

        when(informalCourtesyAddressResolver.resolveAddresses("recipientId", "senderId"))
                .thenReturn(List.of(smsAddress));

        dispatcher.dispatch(notification, 0);

        verify(schedulerService, never()).scheduleEvent(any(), any(), any(), any(), any());
    }
}