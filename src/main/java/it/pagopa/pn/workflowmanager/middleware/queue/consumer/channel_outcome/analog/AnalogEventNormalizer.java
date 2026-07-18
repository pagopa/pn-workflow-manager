package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.NormalizedChannelOutcome;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.SendEventInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AnalogEventNormalizer implements ChannelOutcomeNormalizer<SendEventInt> {
    private final TimelineUtils timelineUtils;
    private final AuditLogService auditLogService;

    @Override
    public NormalizedChannelOutcome normalize(SendEventInt sendEvent,
                                              NotificationInt notification,
                                              SendRelatedTimelineElement sourceSendRequestDetails
    ) {
        SendAnalogMessageDetailsInt analogSendMessageDetails = (SendAnalogMessageDetailsInt) sourceSendRequestDetails;
        int recIndex = analogSendMessageDetails.getRecIndex();

        String statusEventCode = sendEvent.getStatusDescription();

        AnalogEventClassification classification = AnalogEventClassification.fromStatusEventCode(statusEventCode);

        TimelineElementInternal timelineElement = buildTimelineElement(sendEvent, notification, analogSendMessageDetails, classification);

        return NormalizedChannelOutcome.builder()
                .iun(notification.getIun())
                .recIndex(recIndex)
                .classification(classification)
                .channel(ChannelType.ANALOG)
                .timelineElementInternal(timelineElement)
                .originalEventType(statusEventCode)
                .eventTimestamp(sendEvent.getStatusDateTime())
                .pnAuditLogEvent(buildAuditLog(sendEvent, notification, recIndex, analogSendMessageDetails))
                .build();
    }

    private TimelineElementInternal buildTimelineElement(SendEventInt sendEventInt,
                                                         NotificationInt notification,
                                                         SendAnalogMessageDetailsInt analogSendMessageDetails,
                                                         AnalogEventClassification classification
    ) {
        int recIndex = analogSendMessageDetails.getRecIndex();

        return switch(classification.getCategory()) {
            case ChannelOutcomeCategory.Progress ignore -> timelineUtils.buildSendAnalogProgressNotificationTimelineElement(
                    notification,
                    recIndex,
                    sendEventInt,
                    analogSendMessageDetails
            );
            case ChannelOutcomeCategory.Feedback feedback -> timelineUtils.buildSendAnalogFeedbackNotificationTimelineElement(
                    notification,
                    recIndex,
                    sendEventInt,
                    analogSendMessageDetails,
                    determineStatus(feedback)
            );
        };
    }

    private PnAuditLogEvent buildAuditLog(SendEventInt sendEvent, NotificationInt notification, int recIndex, SendAnalogMessageDetailsInt analogSendMessageDetails) {
        String attachments = sendEvent.getAttachments()==null?"":sendEvent.getAttachments().stream().map(AttachmentDetailsInt::getUrl).collect(Collectors.joining(","));
        String msg = String.format(
                "Analog workflow Paper channel execute response requestId=%s statusCode=%s sentAttemptMade=%d attachments=%s relatedRequestId=%s",
                sendEvent.getPrepareRequestId(),
                sendEvent.getStatusCode(),
                analogSendMessageDetails.getSentAttemptMade(),
                attachments,
                analogSendMessageDetails.getRelatedRequestId()
        );
        return auditLogService.buildAuditLogEvent(notification.getIun(), recIndex, PnAuditLogEventType.AUD_COM_PD_EXECUTE_RECEIVE, msg);
    }

    private ResponseStatusInt determineStatus(ChannelOutcomeCategory.Feedback feedback) {
        return feedback.isNegativeFeedback() ? ResponseStatusInt.KO : ResponseStatusInt.OK;
    }
}
