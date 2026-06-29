package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
        // TODO: scommenta
//        timelineService.addTimelineElement(
//                timelineUtils.buildSendDigitalMessageTimelineElement(
//                        notificationInt,
//                        elementId,
//                        recIndex,
//                        digitalAddress,
//                        digitalAddressChannel,
//                        digitalAddressSource
//                ),
//                notificationInt
//        );
    }
}
