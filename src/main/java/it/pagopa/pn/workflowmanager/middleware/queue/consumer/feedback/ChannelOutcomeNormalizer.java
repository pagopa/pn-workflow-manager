package it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;

public interface ChannelOutcomeNormalizer<T> {
    NormalizedChannelOutcome normalize(T rawEvent, NotificationInt notificationInt, int recIndex);
}