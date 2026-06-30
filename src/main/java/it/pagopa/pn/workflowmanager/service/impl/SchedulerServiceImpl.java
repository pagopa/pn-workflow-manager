package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.middleware.queue.actionspool.ActionsPool;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {
    private final ActionsPool actionsPool;

    @Override
    public void scheduleEvent(String iun, Instant dateToSchedule, ActionType actionType) {
        this.scheduleEvent(iun, null, dateToSchedule, actionType, null, null);
    }

    @Override
    public void scheduleEvent(String iun, Integer recIndex, Instant dateToSchedule, ActionType actionType, ActionDetails actionDetails) {
        this.scheduleEvent(iun, recIndex, dateToSchedule, actionType, null, actionDetails);
    }

    @Override
    public void scheduleEvent(
            String iun,
            Integer recIndex,
            Instant dateToSchedule,
            ActionType actionType,
            String timelineEventId,
            ActionDetails actionDetails
    ) {
        Action action = Action.builder()
                .iun(iun)
                .recipientIndex(recIndex)
                .notBefore(dateToSchedule)
                .type(actionType)
                .timelineId(timelineEventId)
                .details(actionDetails)
                .communicationType(CommunicationType.INFORMAL)
                .build();

        action = action.toBuilder()
                .actionId(action.getType().buildActionId(action))
                .build();

        log.debug("ScheduleEvent iun={} recIndex={} dateToSchedule={} actionType={} timelineEventId={} actionId={}", iun, recIndex, dateToSchedule, actionType, timelineEventId, action.getActionId());
        actionsPool.addOnlyAction(action);

    }
}
