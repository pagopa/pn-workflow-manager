package it.pagopa.pn.workflowmanager.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchOrchestrator;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PublicRegistryCallDetailsInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.utils.PublicRegistryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NationalRegistriesResponseHandlerTest {

    @Mock
    private PublicRegistryUtils publicRegistryUtils;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AddressSearchUtils searchUtils;
    @Mock
    private AddressSearchOrchestrator addressSearchOrchestrator;
    @Mock
    private TimelineUtils timelineUtils;

    private NationalRegistriesResponseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NationalRegistriesResponseHandler(
                publicRegistryUtils,
                notificationService,
                searchUtils,
                addressSearchOrchestrator,
                timelineUtils
        );
    }

    @Test
    void handleResponseSchedulesSendChannelMessageWhenDigitalAddressIsPresent() {
        String correlationId = "timeline-IUN-001";
        String iun = "IUN-001";
        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .sentAt(Instant.parse("2026-09-15T10:00:00Z"))
                .build();
        PublicRegistryCallDetailsInt callDetails = PublicRegistryCallDetailsInt.builder()
                .recIndex(2)
                .contactPhase(ContactPhaseInt.SEND_ATTEMPT)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .build();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId(correlationId)
                .digitalAddress(InformalDigitalAddressInt.builder().build())
                .build();

        when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(publicRegistryUtils.getPublicRegistryCallDetail(iun, correlationId)).thenReturn(callDetails);

        handler.handleResponse(response);

        verify(publicRegistryUtils).addPublicRegistryResponseToTimeline(notification, 2, response);
        verify(timelineUtils).addAvailabilitySourceToTimeline(2, notification, DigitalAddressSourceInt.GENERAL, true, response.getDigitalAddress());
        verify(searchUtils).scheduleSendChannelMessageAction(
                eq(new AddressSearchContext(ChannelType.PEC, notification.getSentAt(), notification, 2, null)),
                eq(DigitalAddressSourceInt.GENERAL)
        );
        verifyNoInteractions(addressSearchOrchestrator);
    }

    @Test
    void handleResponseDelegatesToAddressSearchWhenDigitalAddressIsMissing() {
        String correlationId = "timeline-IUN-002";
        String iun = "IUN-002";
        Instant sentAt = Instant.parse("2026-09-15T10:15:00Z");
        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .sentAt(sentAt)
                .build();
        PublicRegistryCallDetailsInt callDetails = PublicRegistryCallDetailsInt.builder()
                .recIndex(1)
                .contactPhase(ContactPhaseInt.SEND_ATTEMPT)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .build();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId(correlationId)
                .build();

        when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(publicRegistryUtils.getPublicRegistryCallDetail(iun, correlationId)).thenReturn(callDetails);

        handler.handleResponse(response);

        ArgumentCaptor<AddressSearchContext> contextCaptor = ArgumentCaptor.forClass(AddressSearchContext.class);
        verify(addressSearchOrchestrator).handle(contextCaptor.capture());
        assertThat(contextCaptor.getValue())
                .isEqualTo(new AddressSearchContext(ChannelType.PEC, sentAt, notification, 1, null));
        verify(searchUtils, never()).scheduleSendChannelMessageAction(org.mockito.Mockito.any(), org.mockito.Mockito.any());
    }

    @Test
    void handleResponseThrowsWhenContactPhaseIsInvalid() {
        String correlationId = "timeline-IUN-003";
        String iun = "IUN-003";
        NotificationInt notification = NotificationInt.builder().iun(iun).build();
        PublicRegistryCallDetailsInt callDetails = PublicRegistryCallDetailsInt.builder()
                .recIndex(5)
                .contactPhase(ContactPhaseInt.CHOOSE_DELIVERY)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .build();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId(correlationId)
                .build();

        when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(publicRegistryUtils.getPublicRegistryCallDetail(iun, correlationId)).thenReturn(callDetails);

        PnInternalException exception = assertThrows(PnInternalException.class, () -> handler.handleResponse(response));

        assertThat(exception.getProblem().getDetail()).contains("Specified contactPhase");
        verify(publicRegistryUtils).addPublicRegistryResponseToTimeline(notification, 5, response);
        verify(timelineUtils, never()).addAvailabilitySourceToTimeline(
                org.mockito.Mockito.anyInt(),
                org.mockito.Mockito.any(),
                org.mockito.Mockito.any(),
                org.mockito.Mockito.anyBoolean(),
                org.mockito.Mockito.any()
        );
    }
}
