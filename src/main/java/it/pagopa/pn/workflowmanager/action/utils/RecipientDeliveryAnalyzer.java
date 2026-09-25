package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageFeedbackDetailsInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK;

@Component
@AllArgsConstructor
@Slf4j
public class RecipientDeliveryAnalyzer {
    private final TimelineUtils timelineUtils;

    //Canali per i quali è possibile l'assenza di un indirizzo di recapito.
    private static final Set<ChannelType> VOLATILE_CHANNELS = Set.of(
            ChannelType.IO,
            ChannelType.EMAIL,
            ChannelType.SMS,
            ChannelType.PEC
    );

    public RecipientDeliveryInfo getDeliveryInfo(List<TimelineElementInternal> timelineElements,
                                                 Campaign campaign,
                                                 int recIndex, RecipientTypeInt recipientType) {
        Optional<String> reachedElementId = timelineUtils.findFirstReachedElementId(timelineElements, recIndex);
        if (reachedElementId.isPresent()) {
            return new RecipientDeliveryInfo(RecipientDeliveryStatus.REACHED, reachedElementId.get());
        } else if (isRecipientUndeliverable(timelineElements, recIndex, campaign, recipientType)) {
            return new RecipientDeliveryInfo(RecipientDeliveryStatus.UNDELIVERABLE);
        } else {
            return new RecipientDeliveryInfo(RecipientDeliveryStatus.UNREACHED);
        }
    }

    private boolean isRecipientUndeliverable(
            List<TimelineElementInternal> timelineElements,
            int recIndex,
            Campaign campaign,
            RecipientTypeInt recipientType
    ) {
        List<ChannelType> activeChannels = campaign.getWorkflowsByRecipientType(recipientType)
                .stream()
                .map(WorkFlowEntity::getChannel)
                .toList();


        if (activeChannels.isEmpty()) {
            throw new PnWorkflowException("Workflow configuration error: no active channels for recipient type " + recipientType);
        }

        boolean hasOnlyVolatileChannels = VOLATILE_CHANNELS.containsAll(activeChannels);
        if (!hasOnlyVolatileChannels) {
            return false;
        }

        return activeChannels.stream()
                .allMatch(channel -> hasChannelBeenSkipped(timelineElements, recIndex, channel));
    }

    /**
     * Metodo centralizzato che verifica lo skip in base al tipo di canale.
     */
    private static boolean hasChannelBeenSkipped(
            List<TimelineElementInternal> timelineElements,
            int recIndex,
            ChannelType channel
    ) {
        return switch (channel) {
            case ChannelType.IO -> hasAppIoFeedbackInTimeline(timelineElements, recIndex);
            case ChannelType.EMAIL -> missingSendDigitalMessageInTimeline(timelineElements, recIndex, DigitalChannelsInt.EMAIL);
            case ChannelType.SMS -> missingSendDigitalMessageInTimeline(timelineElements, recIndex, DigitalChannelsInt.SMS);
            case ChannelType.PEC -> missingSendDigitalMessageInTimeline(timelineElements, recIndex, DigitalChannelsInt.PEC);
            default -> false;
        };
    }

    private static boolean hasAppIoFeedbackInTimeline(List<TimelineElementInternal> timelineElements, int recIndex) {
        return timelineElements.stream()
                .filter(e -> SEND_DIGITAL_MESSAGE_FEEDBACK.equals(e.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(SendDigitalMessageFeedbackDetailsInt.class::isInstance)
                .map(SendDigitalMessageFeedbackDetailsInt.class::cast)
                .anyMatch(d -> d.getRecIndex() == recIndex && DigitalChannelsInt.IO.equals(d.getChannel()));
    }

    private static boolean missingSendDigitalMessageInTimeline(
            List<TimelineElementInternal> timelineElements,
            int recIndex,
            DigitalChannelsInt channel
    ) {
        return timelineElements.stream()
                .filter(e -> SEND_DIGITAL_MESSAGE.equals(e.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(SendDigitalMessageDetailsInt.class::isInstance)
                .map(SendDigitalMessageDetailsInt.class::cast)
                .noneMatch(d -> d.getRecIndex() == recIndex && channel.equals(d.getChannel()));
    }
}
