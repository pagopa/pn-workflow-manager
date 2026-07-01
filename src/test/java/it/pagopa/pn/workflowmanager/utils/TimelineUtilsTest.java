package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.CoverpageCreationRequestDetailsInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineUtilsTest {

    @Mock
    private TimelineService timelineService;

    @InjectMocks
    private TimelineUtils timelineUtils;

    @Test
    void retrieveCoverpageFileKeyReturnsFileKeyFromTimelineElement() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );
        String expectedFileKey = "coverpage-file-key";

        TimelineElementInternal timelineElement = TimelineElementInternal.builder()
                .details(CoverpageCreationRequestDetailsInt.builder()
                        .recIndex(recIndex)
                        .fileKey(expectedFileKey)
                        .build())
                .build();

        when(timelineService.getTimelineElementStrongly(iun, expectedTimelineId))
                .thenReturn(Optional.of(timelineElement));

        String result = timelineUtils.retrieveCoverpageFileKey(iun, recIndex);

        assertEquals(expectedFileKey, result);
        verify(timelineService).getTimelineElementStrongly(iun, expectedTimelineId);
    }

    @Test
    void retrieveCoverpageFileKeyThrowsWhenTimelineElementIsMissing() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        when(timelineService.getTimelineElementStrongly(iun, expectedTimelineId))
                .thenReturn(Optional.empty());

        assertThrows(
                PnInternalException.class,
                () -> timelineUtils.retrieveCoverpageFileKey(iun, recIndex)
        );
    }

    @Test
    void retrieveCoverpageFileKeyThrowsWhenFileKeyIsBlank() {
        String iun = "IUN_123";
        int recIndex = 1;
        String expectedTimelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        TimelineElementInternal timelineElement = TimelineElementInternal.builder()
                .details(CoverpageCreationRequestDetailsInt.builder()
                        .recIndex(recIndex)
                        .fileKey(" ")
                        .build())
                .build();

        when(timelineService.getTimelineElementStrongly(iun, expectedTimelineId))
                .thenReturn(Optional.of(timelineElement));

        assertThrows(
                PnInternalException.class,
                () -> timelineUtils.retrieveCoverpageFileKey(iun, recIndex)
        );
    }
}
