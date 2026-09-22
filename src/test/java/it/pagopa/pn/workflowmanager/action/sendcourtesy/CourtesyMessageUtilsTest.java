package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourtesyMessageUtilsTest {

    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private CourtesyMessageUtils courtesyMessageUtils;

    @Test
    void getSendCourtesyTimelineElementIdBuildsExpectedEventId() {
        String eventId = CourtesyMessageUtils.getSendCourtesyTimelineElementId(0, "IUN-1",
                CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL, Boolean.FALSE);

        org.junit.jupiter.api.Assertions.assertTrue(eventId.contains("IUN-1"));
    }

    @Test
    void addSendCourtesyMessageToTimelineDelegatesToTimelineService() {
        NotificationInt notification = NotificationInt.builder().iun("IUN-1").build();
        CourtesyDigitalAddressInt address = CourtesyDigitalAddressInt.builder()
                .type(CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL)
                .address("a@b.it")
                .build();

        courtesyMessageUtils.addSendCourtesyMessageToTimeline(notification, 0, address, Instant.EPOCH, "event-1", null);

        verify(timelineUtils).buildSendCourtesyMessageTimelineElement(0, notification, address, Instant.EPOCH, "event-1", null);
        verify(timelineService).addTimelineElement(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(notification));
    }
}
