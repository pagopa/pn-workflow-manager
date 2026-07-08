package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;

public interface ChannelOutcomeNormalizer<E extends ChannelOutcomeEvent> {
    NormalizedChannelOutcome normalize(E rawEvent, NotificationInt notificationInt, SendRelatedTimelineElement sourceSendRequestDetails);
}