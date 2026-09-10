package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;


@Component
@Slf4j
@AllArgsConstructor
public class PublicRegistryUtils {

    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;

    public String generateCorrelationId(String iun, Integer recIndex, ContactPhaseInt contactPhase, int sentAttemptMade, DeliveryModeInt deliveryMode) {
        return TimelineEventId.NATIONAL_REGISTRY_CALL.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .deliveryMode(deliveryMode)
                        .contactPhase(contactPhase)
                        .sentAttemptMade(sentAttemptMade)
                        .build());
    }

    public void addPublicRegistryCallToTimeline(NotificationInt notification,
                                                Integer recIndex,
                                                ContactPhaseInt contactPhase,
                                                int sentAttemptMade,
                                                String correlationId,
                                                DeliveryModeInt digital,
                                                String relatedFeedbackTimelineId) {
        addTimelineElement(
                timelineUtils.buildPublicRegistryCallTimelineElement(
                        notification,
                        recIndex,
                        correlationId,
                        digital,
                        contactPhase,
                        sentAttemptMade,
                        relatedFeedbackTimelineId
                ),
                notification
        );
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        timelineService.addTimelineElement(element, notification);
    }

}
