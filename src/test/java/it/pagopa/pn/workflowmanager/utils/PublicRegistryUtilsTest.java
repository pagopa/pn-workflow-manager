package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PublicRegistryCallDetailsInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRegistryUtilsTest {

    @Mock
    private TimelineService timelineService;
    @Mock
    private TimelineUtils timelineUtils;

    private PublicRegistryUtils utils;

    @BeforeEach
    void setUp() {
        utils = new PublicRegistryUtils(timelineService, timelineUtils);
    }

    @Test
    void getPublicRegistryCallDetailReturnsTimelineDetailsWhenPresent() {
        String iun = "IUN-PR-001";
        String correlationId = "NATIONAL_REGISTRY_CALL|IUN-PR-001|1|DIGITAL|SEND_ATTEMPT|0";
        PublicRegistryCallDetailsInt expected = PublicRegistryCallDetailsInt.builder()
                .recIndex(1)
                .contactPhase(ContactPhaseInt.SEND_ATTEMPT)
                .deliveryMode(DeliveryModeInt.DIGITAL)
                .build();
        when(timelineService.getTimelineElementDetails(iun, correlationId, PublicRegistryCallDetailsInt.class))
                .thenReturn(Optional.of(expected));

        PublicRegistryCallDetailsInt result = utils.getPublicRegistryCallDetail(iun, correlationId);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getPublicRegistryCallDetailThrowsWhenMissing() {
        String iun = "IUN-PR-002";
        String correlationId = "missing";
        when(timelineService.getTimelineElementDetails(iun, correlationId, PublicRegistryCallDetailsInt.class))
                .thenReturn(Optional.empty());

        PnInternalException exception = assertThrows(PnInternalException.class,
                () -> utils.getPublicRegistryCallDetail(iun, correlationId));

        assertThat(exception.getProblem().getDetail()).contains(iun).contains(correlationId);
    }

    @Test
    void generateCorrelationIdBuildsNationalRegistryEventId() {
        String iun = "IUN-PR-003";

        String result = utils.generateCorrelationId(iun, 2, ContactPhaseInt.SEND_ATTEMPT, 3, DeliveryModeInt.DIGITAL);

        String expected = TimelineEventId.NATIONAL_REGISTRY_CALL.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(2)
                        .deliveryMode(DeliveryModeInt.DIGITAL)
                        .contactPhase(ContactPhaseInt.SEND_ATTEMPT)
                        .sentAttemptMade(3)
                        .build()
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void addPublicRegistryCallToTimelinePersistsBuiltElement() {
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-PR-004")
                .sentAt(Instant.parse("2026-09-15T10:00:00Z"))
                .build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().elementId("timeline-id").build();
        when(timelineUtils.buildPublicRegistryCallTimelineElement(
                eq(notification),
                eq(0),
                eq("corr-1"),
                eq(DeliveryModeInt.DIGITAL),
                eq(ContactPhaseInt.SEND_ATTEMPT),
                eq(1),
                eq("feedback-id")
        )).thenReturn(timelineElement);

        utils.addPublicRegistryCallToTimeline(notification, 0, ContactPhaseInt.SEND_ATTEMPT, 1, "corr-1",
                DeliveryModeInt.DIGITAL, "feedback-id");

        verify(timelineService).addTimelineElement(timelineElement, notification);
    }

    @Test
    void addPublicRegistryResponseToTimelinePersistsBuiltElement() {
        NotificationInt notification = NotificationInt.builder()
                .iun("IUN-PR-005")
                .sentAt(Instant.parse("2026-09-15T10:00:00Z"))
                .build();
        NationalRegistriesResponse response = NationalRegistriesResponse.builder()
                .correlationId("corr-2")
                .digitalAddress(InformalDigitalAddressInt.builder().build())
                .build();
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().elementId("timeline-response").build();
        when(timelineUtils.buildPublicRegistryResponseCallTimelineElement(notification, 1, response))
                .thenReturn(timelineElement);

        utils.addPublicRegistryResponseToTimeline(notification, 1, response);

        verify(timelineService).addTimelineElement(timelineElement, notification);
    }
}
