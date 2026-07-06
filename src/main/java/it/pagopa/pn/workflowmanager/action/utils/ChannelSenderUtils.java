package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelSenderUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    public static String buildSendDigitalMessageEventId(String iun, int recIndex, @Nonnull ChannelType channel) {
        return TimelineEventId.SEND_DIGITAL_MESSAGE.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );
    }

    public static InformalDigitalAddressInt buildDigitalAddress(String address, InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE type) {
        return InformalDigitalAddressInt.builder()
                .address(address)
                .type(type)
                .build();
    }

    public void saveSendDigitalMessageElement(
        NotificationInt notificationInt,
        String elementId,
        int recIndex,
        InformalDigitalAddressInt digitalAddress,
        DigitalChannelsInt digitalAddressChannel,
        DigitalAddressSourceInt digitalAddressSource
    ) {
        timelineService.addTimelineElement(
                timelineUtils.buildSendDigitalMessageTimelineElement(
                        notificationInt,
                        elementId,
                        recIndex,
                        digitalAddress,
                        digitalAddressChannel,
                        digitalAddressSource
                ),
                notificationInt
        );
    }
    public boolean searchIfUserFromAppIo(String iun, ChannelType channelType, int recIndex){
        String eventId = buildDeliveredEventId(iun, recIndex, channelType);
        Optional<TimelineElementInternal> timelineElementInternal  = timelineService.getTimelineElement(iun, eventId);
        if(timelineElementInternal.isPresent()){
            log.debug("User from App IO found - iun={} recIndex={} eventId={}", iun, recIndex, eventId);
            return true;
        } else {
            log.debug("User from App IO not found - iun={} recIndex={} eventId={}", iun, recIndex, eventId);
            return false;
        }
    }

    public static String buildDeliveredEventId(String iun, int recIndex, @Nonnull ChannelType channel) {
        return TimelineEventId.DELIVERED.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );
    }

    public void saveSendDigitalMessageSkipElement(int recIndex,
                                                  NotificationInt notification,
                                                  String eventId,
                                                  DigitalChannelsInt digitalAddressChannel,
                                                  DigitalAddressSourceInt digitalAddressSource){
        timelineService.addTimelineElement(
                timelineUtils.buildSendDigitalMessageSkipTimelineElement(recIndex,
                        notification,
                        eventId,
                        digitalAddressChannel,
                        digitalAddressSource
                ),
                notification
        );
    }

    public static String buildSendDigitalMessageSkipTimelineElementId(Integer recIndex, String iun, @Nonnull ChannelType channel) {
        return TimelineEventId.SEND_DIGITAL_MESSAGE_SKIP.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .channel(channel.name())
                .build()
        );
    }
}
