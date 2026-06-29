package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.notification.common.CommunicationType;

import java.time.Instant;

public interface SchedulerService {
    void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, CommunicationType communicationType);

    void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule, ActionType actionType, String timelineEventId, ActionDetails actionDetails, CommunicationType communicationType);

    void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails, CommunicationType communicationType);
}
