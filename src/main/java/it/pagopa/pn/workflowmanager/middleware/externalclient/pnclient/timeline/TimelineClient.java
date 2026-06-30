package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.timeline;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NotificationHistoryResponse;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.AddTimelineElementResponse;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.common.TimelineElementDetailsInt;

import java.time.Instant;
import java.util.List;

public interface TimelineClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_TIMELINE_SERVICE;
    String ADD_TIMELINE_ELEMENT = "ADD TIMELINE ELEMENT";
    String RETRIEVE_AND_INCREMENT_COUNTER_FOR_TIMELINE_EVENT = "RETRIEVE AND INCREMENT COUNTER FOR TIMELINE EVENT";
    String GET_TIMELINE_ELEMENT = "GET TIMELINE ELEMENT";
    String GET_TIMELINE_ELEMENT_DETAILS = "GET TIMELINE ELEMENT DETAILS";
    String GET_TIMELINE_ELEMENT_DETAIL_FOR_SPECIFIC_RECIPIENT = "GET TIMELINE ELEMENT DETAIL FOR SPECIFIC RECIPIENT";
    String GET_TIMELINE_ELEMENT_FOR_SPECIFIC_RECIPIENT = "GET TIMELINE ELEMENT FOR SPECIFIC RECIPIENT";
    String GET_TIMELINE = "GET TIMELINE";
    String GET_TIMELINE_AND_STATUS_HISTORY = "GET TIMELINE AND STATUS HISTORY";

    AddTimelineElementResponse addTimelineElement(TimelineElementInternal element, NotificationInt notification);

    Long retrieveAndIncrementCounterForTimelineEvent(String timelineId);

    TimelineElementInternal getTimelineElement(String iun, String timelineId, Boolean strongly);

    TimelineElementDetailsInt getTimelineElementDetails(String iun, String timelineId);

    TimelineElementDetailsInt getTimelineElementDetailForSpecificRecipient(String iun, Integer recIndex, Boolean confidentialInfoRequired, TimelineElementCategoryInt category);

    TimelineElementInternal getTimelineElementForSpecificRecipient(String iun, Integer recIndex, TimelineElementCategoryInt category);

    List<TimelineElementInternal> getTimeline(String iun, Boolean confidentialInfoRequired, Boolean strongly, String timelineId);

    NotificationHistoryResponse getTimelineAndStatusHistory(String iun, int recipients, Instant createdAt);

}
