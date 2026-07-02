package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;

import java.time.Instant;

public interface SchedulerService {
    void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule, ActionType actionType);

    void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule, ActionType actionType, String timelineEventId, ActionDetails actionDetails);

    void scheduleEvent(String iun,Integer recIndex, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails);
}
