package it.pagopa.pn.workflowmanager.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchOrchestrator;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PublicRegistryCallDetailsInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.utils.PublicRegistryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NationalRegistriesResponseHandlerTest {

    @Mock
    private PublicRegistryUtils publicRegistryUtils;
    @Mock
    private NotificationService notificationService;
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
                addressSearchOrchestrator,
                timelineUtils
        );
    }

    @ParameterizedTest(name = "handleResponse resumes orchestrator with {1} outcome")
    @MethodSource("digitalAddressScenarios")
    void handleResponseResumesOrchestratorWithExpectedOutcome(InformalDigitalAddressInt digitalAddress,
                                                              String scenarioName,
                                                              SourceSearchOutcome expectedOutcome) {
        String correlationId = "timeline-" + scenarioName;
        String iun = "IUN-" + scenarioName;
        Instant sentAt = Instant.parse("2026-09-15T10:00:00Z");
        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .sentAt(sentAt)
                .build();
        PublicRegistryCallDetailsInt callDetails = PublicRegistryCallDetailsInt.builder()
                .recIndex(1)
                .contactPhase(ContactPhaseInt.SEND_ATTEMPT)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .sentAttemptMade(0)
                .build();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId(correlationId)
                .digitalAddress(digitalAddress)
                .build();

        when(timelineUtils.getIunFromTimelineId(correlationId)).thenReturn(iun);
        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notification);
        when(publicRegistryUtils.getPublicRegistryCallDetail(iun, correlationId)).thenReturn(callDetails);

        handler.handleResponse(response);

        verify(publicRegistryUtils).addPublicRegistryResponseToTimeline(notification, 1, response);
        verify(addressSearchOrchestrator).resume(
                eq(new AddressSearchContext(ChannelType.PEC, sentAt, notification, 1, 0)),
                eq(DigitalAddressSourceInt.GENERAL),
                eq(expectedOutcome)
        );
    }

    private static Stream<Arguments> digitalAddressScenarios() {
        InformalDigitalAddressInt digitalAddress = InformalDigitalAddressInt.builder().build();
        return Stream.of(
                Arguments.of(
                        digitalAddress, "present",
                        SourceSearchOutcome.found(DigitalAddressSourceInt.GENERAL, digitalAddress)),
                Arguments.of(
                        null, "missing",
                        SourceSearchOutcome.notFound(DigitalAddressSourceInt.GENERAL))
        );
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
