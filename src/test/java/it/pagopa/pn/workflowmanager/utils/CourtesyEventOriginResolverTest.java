package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendCourtesyMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.CourtesyMessageProgressEvent;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourtesyEventOriginResolverTest {
    @Mock
    private TimelineUtils timelineUtils;

    @Mock
    private TimelineService timelineService;

    private CourtesyEventOriginResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new CourtesyEventOriginResolver(timelineUtils, timelineService);
    }

    @Test
    void resolveOriginReturnsCourtesyMessageWhenDetailsAreCourtesyAddressRelated() {
        String requestId = "requestId-1";
        String iun = "IUN-001";
        CourtesyMessageProgressEvent event = new CourtesyMessageProgressEvent();
        event.setRequestId(requestId);

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        // Implementa interfaccia CourtesyAddressRelatedTimelineElement
        SendCourtesyMessageDetailsInt details = mock(SendCourtesyMessageDetailsInt.class);
        when(timelineElement.getDetails()).thenReturn(details);
        when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);
        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(timelineElement));

        CourtesyEventOriginResolver.CourtesyEventOrigin origin = resolver.resolveOrigin(event);

        assertEquals(CourtesyEventOriginResolver.CourtesyEventOrigin.COURTESY_MESSAGE, origin);
    }

    @Test
    void resolveOriginReturnsChannelMessageWhenDetailsAreSendRelated() {
        String requestId = "requestId-2";
        String iun = "IUN-002";
        CourtesyMessageProgressEvent event = new CourtesyMessageProgressEvent();
        event.setRequestId(requestId);

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        SendRelatedTimelineElement details = mock(SendRelatedTimelineElement.class);
        when(timelineElement.getDetails()).thenReturn(details);
        when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);
        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(timelineElement));

        CourtesyEventOriginResolver.CourtesyEventOrigin origin = resolver.resolveOrigin(event);

        assertEquals(CourtesyEventOriginResolver.CourtesyEventOrigin.CHANNEL_MESSAGE, origin);
    }

    @Test
    void resolveOriginThrowsExceptionWhenTimelineElementIsNotFound() {
        String requestId = "requestId-3";
        String iun = "IUN-003";
        CourtesyMessageProgressEvent event = new CourtesyMessageProgressEvent();
        event.setRequestId(requestId);

        when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);
        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.empty());

        assertThrows(PnInternalException.class, () -> resolver.resolveOrigin(event));
    }

    @Test
    void resolveOriginThrowsExceptionWhenDetailsAreOfUnexpectedType() {
        String requestId = "requestId-4";
        String iun = "IUN-004";
        CourtesyMessageProgressEvent event = new CourtesyMessageProgressEvent();
        event.setRequestId(requestId);

        TimelineElementInternal timelineElement = mock(TimelineElementInternal.class);
        TimelineElementDetailsInt details = mock(TimelineElementDetailsInt.class);
        when(timelineElement.getDetails()).thenReturn(details);
        when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);
        when(timelineService.getTimelineElement(iun, requestId)).thenReturn(Optional.of(timelineElement));

        assertThrows(PnInternalException.class, () -> resolver.resolveOrigin(event));
    }
}