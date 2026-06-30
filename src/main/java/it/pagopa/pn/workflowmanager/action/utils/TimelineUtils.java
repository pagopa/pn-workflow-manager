package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementDetailsInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimelineUtils {

    public TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 @NotNull TimelineElementDetailsInt details) {

        return TimelineElementInternal.builder()
                .iun(notification.getIun())
                .category(category)
                .timestamp(Instant.now())
                .elementId(elementId)
                .details(details)
                .paId(notification.getSender().getPaId())
                .notificationSentAt(notification.getSentAt())
                .build();
    }

    public TimelineElementInternal buildSendDigitalMessageTimelineElement(
            NotificationInt notificationInt,
            String elementId,
            int recIndex,
            InformalDigitalAddressInt digitalAddress,
            DigitalChannelsInt digitalAddressChannel,
            DigitalAddressSourceInt digitalAddressSource
    ){
        SendDigitalMessageDetailsInt detailsInt = SendDigitalMessageDetailsInt.builder()
                .recIndex(recIndex)
                .digitalAddress(digitalAddress)
                .channel(digitalAddressChannel)
                .digitalAddressSource(digitalAddressSource)
                .build();

        return buildTimeline(notificationInt, TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, elementId, detailsInt);
    }
}
