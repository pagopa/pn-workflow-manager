package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.io.IoSendMessageResultInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourtesyMessageUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;

    public static String getSendCourtesyTimelineElementId(Integer recIndex, String iun, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT courtesyAddressType, Boolean optin) {
        return TimelineEventId.SEND_COURTESY_MESSAGE.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .courtesyAddressType(courtesyAddressType)
                .optin(optin)
                .build()
        );
    }

    public void addSendCourtesyMessageToTimeline(NotificationInt notification, Integer recIndex, CourtesyDigitalAddressInt courtesyAddress, Instant sentDate, String eventId,
                                                 IoSendMessageResultInt ioSendMessageResult) {
        addTimelineElement(
                timelineUtils.buildSendCourtesyMessageTimelineElement(recIndex, notification, courtesyAddress, sentDate, eventId, ioSendMessageResult),
                notification
        );
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        timelineService.addTimelineElement(element, notification);
    }

}
