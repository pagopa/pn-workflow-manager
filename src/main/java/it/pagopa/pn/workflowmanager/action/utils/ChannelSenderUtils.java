package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelSenderUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final AttachmentUtils attachmentUtils;

    public static String buildSendDigitalMessageEventId(String iun, int recIndex, @Nonnull ChannelType channel,Integer sentAttemptMade) {
        return TimelineEventId.SEND_DIGITAL_MESSAGE.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .sentAttemptMade(sentAttemptMade)
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

    public void saveCoverpageCreationElement(
            NotificationInt notificationInt,
            int recIndex,
            String fileKey
    ) {
        timelineService.addTimelineElement(
                timelineUtils.buildCoverpageCreationTimelineElement(
                        recIndex,
                        fileKey,
                        notificationInt
                ),
                notificationInt
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

    public void savePrepareAnalogDeliveryElement(
            Integer recIndex,
            NotificationInt notification,
            String elementId,
            ServiceLevelInt serviceLevel,
            Integer sentAttemptMade,
            String relatedRequestId,
            PhysicalAddressInt physicalAddressInt
    ) {
        timelineService.addTimelineElement(
                timelineUtils.buildPrepareAnalogDeliveryTimelineElement(
                        recIndex,
                        notification,
                        elementId,
                        serviceLevel,
                        sentAttemptMade,
                        relatedRequestId,
                        physicalAddressInt
                ),
                notification
        );
    }

    public static String buildPrepareAnalogDeliveryTimelineElementId(Integer recIndex, String iun, Integer sentAttemptMade) {
        return TimelineEventId.PREPARE_ANALOG_DELIVERY.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .deliveryType(AnalogDeliveryTypeInt.RS.name())
                .sentAttemptMade(sentAttemptMade)
                .build()
        );
    }

    public List<String> resolveAttachmentsForChannel(NotificationInt notification, int recIndex, Campaign campaign, ChannelType channel) {

        WorkFlowEntity currentWorkflowStep = campaign.getWorkflowByChannel(channel);

        boolean includeAttachment = Boolean.TRUE.equals(currentWorkflowStep.getIncludeAttachment());
        if (!includeAttachment) {
            return List.of();
        }
        return attachmentUtils.retrieveAttachments(
                notification, recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, channel),
                false
        );
    }
}
