package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.timeline;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.api.TimelineControllerApi;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.*;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.AddTimelineElementResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.workflowmanager.service.mapper.TimelineServiceMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineClientImplTest {
    @Mock
    private TimelineControllerApi timelineControllerApi;

    @Mock
    private TimelineServiceMapper timelineServiceMapper;

    @InjectMocks
    private TimelineClientImpl timelineServiceClient;

    @Test
    void addTimelineElementReturnsTrueWhenConflictOccurs() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        when(timelineElementInternal.getElementId()).thenReturn("elementId");
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);

        NewTimelineElement newTimelineElement = Mockito.mock(NewTimelineElement.class);
        when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        PnHttpResponseException exception = new PnHttpResponseException("Conflict", HttpStatus.SC_CONFLICT);

        Mockito.doThrow(exception)
                .when(timelineControllerApi)
                .addTimelineElement(newTimelineElement);

        AddTimelineElementResponse result = timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt);
        assertEquals(timelineElementInternal.getElementId(), result.getTimelineElementId());
        assertTrue(result.isDuplicate());
    }

    @Test
    void addTimelineElementReturnsFalseWhenOtherErrorOccurs() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);

        NewTimelineElement newTimelineElement = new NewTimelineElement();
        when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        when(timelineControllerApi.addTimelineElement(Mockito.any()))
                .thenThrow(new RuntimeException("error generic"));

        Assertions.assertThrows(RuntimeException.class, () -> timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt));

    }

    @Test
    void addTimelineElement_throwsExceptionOnError() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);
        NewTimelineElement newTimelineElement = Mockito.mock(NewTimelineElement.class);
        PnHttpResponseException exception = new PnHttpResponseException("Errore generico", HttpStatus.SC_INTERNAL_SERVER_ERROR);

        when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);

        Mockito.doThrow(exception)
                .when(timelineControllerApi)
                .addTimelineElement(newTimelineElement);

        PnHttpResponseException thrown = assertThrows(PnHttpResponseException.class, () ->
                timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt)
        );

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, thrown.getStatusCode());
    }

    @Test
    void retrieveAndIncrementCounterForTimelineEvent_returnsExpectedCounter() {
        String timelineId = "timeline123";
        Long expectedCounter = 42L;

        when(timelineControllerApi.retrieveAndIncrementCounterForTimelineEvent(timelineId))
                .thenReturn(expectedCounter);

        Long result = timelineServiceClient.retrieveAndIncrementCounterForTimelineEvent(timelineId);

        assertEquals(expectedCounter, result);
        Mockito.verify(timelineControllerApi).retrieveAndIncrementCounterForTimelineEvent(timelineId);
    }

    @Test
    void getTimelineElement_returnsExpectedElement() {
        String iun = "iun123";
        String timelineId = "timeline123";
        Boolean strongly = true;
        TimelineElementInternal expectedElement = Mockito.mock(TimelineElementInternal.class);
        TimelineElement timelineElement = new TimelineElement();

        when(timelineControllerApi.getTimelineElement(iun, timelineId, strongly))
                .thenReturn(timelineElement);

        when(timelineServiceMapper.toTimelineElementInternal(timelineElement)).thenReturn(expectedElement);

        TimelineElementInternal result = timelineServiceClient.getTimelineElement(iun, timelineId, strongly);

        assertEquals(expectedElement, result);
        Mockito.verify(timelineControllerApi).getTimelineElement(iun, timelineId, strongly);
    }

    @Test
    void getTimelineElementDetails_returnsExpectedDetails() {
        String iun = "iun123";
        String timelineId = "timeline123";
        TimelineElementDetails timelineElementDetails = new SendDigitalDetails().categoryType("SEND_ANALOG_MESSAGE");
        TimelineElementDetailsInt expectedDetails = Mockito.mock(TimelineElementDetailsInt.class);

        when(timelineControllerApi.getTimelineElementDetails(iun, timelineId))
                .thenReturn(timelineElementDetails);

        when(timelineServiceMapper.toTimelineElementDetailsInt(timelineElementDetails, TimelineElementCategoryInt.SEND_ANALOG_MESSAGE))
                        .thenReturn(expectedDetails);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetails(iun, timelineId);

        assertEquals(expectedDetails, result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetails(iun, timelineId);
    }

    @Test
    void getTimelineElementDetails_handlesNullDetails() {
        String iun = "iun123";
        String timelineId = "timeline123";

        when(timelineControllerApi.getTimelineElementDetails(iun, timelineId))
                .thenReturn(null);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetails(iun, timelineId);

        assertNull(result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetails(iun, timelineId);
        Mockito.verify(timelineServiceMapper, never()).toTimelineElementDetailsInt(Mockito.any(), Mockito.any());
    }

    @Test
    void getTimelineElementDetailForSpecificRecipient_returnsExpectedDetails() {
        String iun = "iun123";
        Integer recIndex = 1;
        Boolean confidentialInfoRequired = true;
        TimelineCategory category = TimelineCategory.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElementDetails timelineElementDetails = new NotificationViewedDetails().categoryType("INFORMAL_NOTIFICATION_VIEWED");
        TimelineElementDetailsInt expectedDetails = Mockito.mock(TimelineElementDetailsInt.class);

        when(timelineControllerApi.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category))
                .thenReturn(timelineElementDetails);

        when(timelineServiceMapper.toTimelineElementDetailsInt(timelineElementDetails, TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED))
                .thenReturn(expectedDetails);

        TimelineElementDetailsInt result = timelineServiceClient.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, categoryInt);

        assertEquals(expectedDetails, result);
        Mockito.verify(timelineControllerApi).getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category);
    }

    @Test
    void getTimelineElementDetailForSpecificRecipient_throwsException() {
        String iun = "iun123";
        Integer recIndex = 1;
        Boolean confidentialInfoRequired = true;
        TimelineCategory category = TimelineCategory.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;

        when(timelineControllerApi.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, category))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimelineElementDetailForSpecificRecipient(iun, recIndex, confidentialInfoRequired, categoryInt)
        );
    }

    @Test
    void getTimelineElementForSpecificRecipient_returnsExpectedElement() {
        String iun = "iun123";
        Integer recIndex = 1;
        TimelineCategory category = TimelineCategory.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElement timelineElement = new TimelineElement();
        TimelineElementInternal expectedElement = Mockito.mock(TimelineElementInternal.class);

        when(timelineControllerApi.getTimelineElementForSpecificRecipient(iun, recIndex, category))
                .thenReturn(timelineElement);

        when(timelineServiceMapper.toTimelineElementInternal(timelineElement)).thenReturn(expectedElement);

        TimelineElementInternal result = timelineServiceClient.getTimelineElementForSpecificRecipient(iun, recIndex, categoryInt);

        assertEquals(expectedElement, result);
        Mockito.verify(timelineControllerApi).getTimelineElementForSpecificRecipient(iun, recIndex, category);
    }

    @Test
    void getTimelineElementForSpecificRecipient_throwsException() {
        String iun = "iun123";
        Integer recIndex = 1;
        TimelineCategory category = TimelineCategory.INFORMAL_NOTIFICATION_VIEWED;
        TimelineElementCategoryInt categoryInt = TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;

        when(timelineControllerApi.getTimelineElementForSpecificRecipient(iun, recIndex, category))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimelineElementForSpecificRecipient(iun, recIndex, categoryInt)
        );
    }

    @Test
    void getTimeline_returnsExpectedList() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = true;
        Boolean strongly = false;
        String timelineId = "timeline123";
        TimelineElement timelineElementKnown = new TimelineElement().category(TimelineCategory.INFORMAL_NOTIFICATION_VIEWED);
        TimelineElement timelineElementUnknown = new TimelineElement().category(TimelineCategory.NORMALIZED_ADDRESS);
        TimelineElementInternal expectedElement = new TimelineElementInternal();

        when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenReturn(List.of(timelineElementKnown, timelineElementUnknown));

        when(timelineServiceMapper.toTimelineElementInternal(timelineElementKnown)).thenReturn(expectedElement);

        List<TimelineElementInternal> result = timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId);

        assertEquals(1, result.size()); // Only one known category should be returned
        assertEquals(expectedElement, result.getFirst());
        Mockito.verify(timelineControllerApi).getTimeline(iun, confidentialInfoRequired, strongly, timelineId);
    }

    @Test
    void getTimeline_throwsException() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = true;
        Boolean strongly = false;
        String timelineId = "timeline123";

        when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenThrow(new RuntimeException("Errore"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId)
        );
    }

    @Test
    void getTimelineAndStatusHistory_returnsExpectedResponse() {
        String iun = "iun123";
        int recipients = 2;
        Instant createdAt = Instant.now();
        NotificationHistoryResponse expectedResponse = new NotificationHistoryResponse();

        when(timelineControllerApi.getTimelineAndStatusHistory(iun, recipients, createdAt))
                .thenReturn(expectedResponse);

        NotificationHistoryResponse result = timelineServiceClient.getTimelineAndStatusHistory(iun, recipients, createdAt);

        assertEquals(expectedResponse, result);
        Mockito.verify(timelineControllerApi).getTimelineAndStatusHistory(iun, recipients, createdAt);
    }

    @Test
    void getTimelineAndStatusHistory_throwsException() {
        String iun = "iun123";
        int recipients = 2;
        Instant createdAt = Instant.now();

        when(timelineControllerApi.getTimelineAndStatusHistory(iun, recipients, createdAt))
                .thenThrow(new RuntimeException("Error fetching history"));

        assertThrows(RuntimeException.class, () ->
                timelineServiceClient.getTimelineAndStatusHistory(iun, recipients, createdAt)
        );
    }

    @Test
    void addTimelineElement_returnsSuccessResponse() {
        TimelineElementInternal timelineElementInternal = Mockito.mock(TimelineElementInternal.class);
        NotificationInt notificationInt = Mockito.mock(NotificationInt.class);
        NewTimelineElement newTimelineElement = Mockito.mock(NewTimelineElement.class);
        String elementId = "element123";
        TimelineElementIdResponse idResponse = new TimelineElementIdResponse();
        idResponse.setElementId(elementId);

        when(timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt))
                .thenReturn(newTimelineElement);
        when(timelineControllerApi.addTimelineElement(newTimelineElement))
                .thenReturn(idResponse);

        AddTimelineElementResponse result = timelineServiceClient.addTimelineElement(timelineElementInternal, notificationInt);

        assertNotNull(result);
        assertEquals(elementId, result.getTimelineElementId());
        assertFalse(result.isDuplicate());
        Mockito.verify(timelineControllerApi).addTimelineElement(newTimelineElement);
    }

    @Test
    void getTimeline_filtersOutUnknownCategories() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = false;
        Boolean strongly = true;
        String timelineId = null;

        TimelineElement knownCategoryElement = new TimelineElement()
                .category(TimelineCategory.SEND_DIGITAL_MESSAGE);
        TimelineElement unknownCategoryElement = new TimelineElement()
                .category(TimelineCategory.AAR_GENERATION);

        TimelineElementInternal mappedElement = new TimelineElementInternal();

        when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenReturn(List.of(knownCategoryElement, unknownCategoryElement));
        when(timelineServiceMapper.toTimelineElementInternal(knownCategoryElement))
                .thenReturn(mappedElement);

        List<TimelineElementInternal> result = timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId);

        assertEquals(1, result.size());
        Mockito.verify(timelineServiceMapper, Mockito.times(1)).toTimelineElementInternal(Mockito.any());
    }

    @Test
    void getTimeline_returnsEmptyListWhenNoKnownCategories() {
        String iun = "iun123";
        Boolean confidentialInfoRequired = false;
        Boolean strongly = false;
        String timelineId = "timeline123";

        TimelineElement unknownCategoryElement = new TimelineElement()
                .category(TimelineCategory.NORMALIZED_ADDRESS);

        when(timelineControllerApi.getTimeline(iun, confidentialInfoRequired, strongly, timelineId))
                .thenReturn(List.of(unknownCategoryElement));

        List<TimelineElementInternal> result = timelineServiceClient.getTimeline(iun, confidentialInfoRequired, strongly, timelineId);

        assertTrue(result.isEmpty());
        Mockito.verify(timelineServiceMapper, never()).toTimelineElementInternal(Mockito.any());
    }

}
