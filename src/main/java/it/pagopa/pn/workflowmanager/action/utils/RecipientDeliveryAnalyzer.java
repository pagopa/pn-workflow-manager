package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageFeedbackDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageSkipDetailsInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;

@Component
@AllArgsConstructor
@Slf4j
public class RecipientDeliveryAnalyzer {
    private final TimelineUtils timelineUtils;

    public RecipientDeliveryStatus getDeliveryStatus(List<TimelineElementInternal> timelineElements,
                                                     Campaign campaign,
                                                     int recIndex, RecipientTypeInt recipientType) {
        if (isRecipientReached(timelineElements,recIndex)) {
            return RecipientDeliveryStatus.REACHED;
        } else if (isRecipientUndeliverable(timelineElements, recIndex, campaign, recipientType)) {
            return RecipientDeliveryStatus.UNDELIVERABLE;
        } else {
            return RecipientDeliveryStatus.UNREACHED;
        }
    }

    private boolean isRecipientReached(List<TimelineElementInternal> timelineElements, int recIndex) {
        return timelineUtils.checkTimelineCategories(timelineElements, recIndex, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);
    }

    private boolean isRecipientUndeliverable(List<TimelineElementInternal> timelineElements, int recIndex,
                                             Campaign campaign, RecipientTypeInt recipientType) {

        List<WorkFlowEntity> filteredWorkflow = campaign.getWorkflow().stream()
                .filter(w -> w.getRecipientType().contains(recipientType))
                .toList();

        boolean hasIO = false;
        boolean hasEmail = false;
        boolean hasSms = false;

        for (WorkFlowEntity workflow : filteredWorkflow) {
            if (ChannelType.IO.equals(workflow.getChannel())) {
                hasIO = true;
            } else if (ChannelType.EMAIL.equals(workflow.getChannel())) {
                hasEmail = true;
            } else if (ChannelType.SMS.equals(workflow.getChannel())) {
                hasSms = true;
            }
        }

        boolean hasAppIoFeedback = hasAppIoFeedbackInTimeline(timelineElements, recIndex, hasIO);

        boolean hasEmailSkip = hasEmailSkipInTimeline(timelineElements, recIndex, hasEmail);

        boolean hasSmsSkip = hasSmsSkipInTimeline(timelineElements, recIndex, hasSms);

        // Il destinatario è undeliverable SOLO SE tutti i canali attivi hanno soddisfatto la loro condizione di skip/feedback
        return hasAppIoFeedback && hasEmailSkip && hasSmsSkip;
    }

    private static boolean hasEmailSkipInTimeline(List<TimelineElementInternal> timelineElements, int recIndex, boolean hasEmail) {
        return hasDigitalChannelSkipInTimeline(timelineElements, recIndex, hasEmail, DigitalChannelsInt.EMAIL);
    }

    private static boolean hasSmsSkipInTimeline(List<TimelineElementInternal> timelineElements, int recIndex, boolean hasSms) {
        return hasDigitalChannelSkipInTimeline(timelineElements, recIndex, hasSms, DigitalChannelsInt.SMS);
    }

    private static boolean hasAppIoFeedbackInTimeline(List<TimelineElementInternal> timelineElements, int recIndex, boolean hasIO) {
        return !hasIO || timelineElements.stream()
                .filter(e -> SEND_DIGITAL_MESSAGE_FEEDBACK.equals(e.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(d -> d instanceof SendDigitalMessageFeedbackDetailsInt)
                .map(d -> (SendDigitalMessageFeedbackDetailsInt) d)
                .anyMatch(d -> d.getRecIndex() == recIndex && DigitalChannelsInt.APPIO.equals(d.getChannel()));
    }

    private static boolean hasDigitalChannelSkipInTimeline(List<TimelineElementInternal> timelineElements,
                                                           int recIndex,
                                                           boolean hasChannel,
                                                           DigitalChannelsInt channel) {
        return !hasChannel || timelineElements.stream()
                .filter(e -> SEND_DIGITAL_MESSAGE_SKIP.equals(e.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(d -> d instanceof SendDigitalMessageSkipDetailsInt)
                .map(d -> (SendDigitalMessageSkipDetailsInt) d)
                .anyMatch(d -> d.getRecIndex() == recIndex && channel.equals(d.getChannel()));
    }
}
