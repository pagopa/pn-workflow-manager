package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ContactPhaseInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PublicRegistryCallDetailsInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_TIMELINEELEMENTFAILED;

@Component
@Slf4j
@AllArgsConstructor
public class PublicRegistryUtils {

    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;

    public PublicRegistryCallDetailsInt getPublicRegistryCallDetail(String iun, String correlationId) {
        //Viene ottenuto l'oggetto di timeline creato in fase d'invio notifica al public registry
        Optional<PublicRegistryCallDetailsInt> optTimeLinePublicRegistrySend = timelineService.getTimelineElementDetails(iun, correlationId, PublicRegistryCallDetailsInt.class);

        if (optTimeLinePublicRegistrySend.isPresent()) {
            return optTimeLinePublicRegistrySend.get();
        } else {
            log.error("There isn't timelineElement - iun {} correlationId {}", iun, correlationId);
            throw new PnInternalException("There isn't timelineElement - iun " + iun + " correlationId " + correlationId, ERROR_CODE_WORKFLOWMANAGER_TIMELINEELEMENTFAILED);
        }
    }

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

    public void addPublicRegistryResponseToTimeline(NotificationInt notification, Integer recIndex, NationalRegistriesResponse response) {
        addTimelineElement( 
                timelineUtils.buildPublicRegistryResponseCallTimelineElement(notification, recIndex, response),
                notification
        );
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        timelineService.addTimelineElement(element, notification);
    }

}
