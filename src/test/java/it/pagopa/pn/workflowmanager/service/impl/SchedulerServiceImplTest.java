package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.middleware.queue.actionspool.ActionsPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class SchedulerServiceImplTest {

    private ActionsPool actionsPool;

    private SchedulerServiceImpl schedulerService;
    
    @BeforeEach
    void setup() {
        actionsPool = Mockito.mock(ActionsPool.class);

        schedulerService = new SchedulerServiceImpl(actionsPool);
    }

    @Test
    void scheduleEvent_withCommunicationType_shouldAddAction() {
        String iun = "IUN01";
        Integer recIndex = 0;
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.END_WORKFLOW;

        schedulerService.scheduleEvent(iun,recIndex, dateToSchedule, actionType);

        Mockito.verify(actionsPool).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_withActionDetails_shouldAddAction() {
        String iun = "IUN01";
        Integer recIndex = 0;
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        Integer recIndex = 0;
        ActionType actionType = ActionType.END_WORKFLOW;
        ActionDetails actionDetails = Mockito.mock(ActionDetails.class);

        schedulerService.scheduleEvent(iun, recIndex, dateToSchedule, actionType, actionDetails);

        Mockito.verify(actionsPool).addOnlyAction(any(Action.class));
    }

    @Test
    void scheduleEvent_fullSignature_shouldBuildActionWithCorrectFields() {
        String iun = "IUN01";
        Integer recIndex = 3;
        Instant dateToSchedule = Instant.parse("2022-08-30T16:04:13.913859900Z");
        ActionType actionType = ActionType.END_WORKFLOW;
        String timelineEventId = "timeline_01";


        schedulerService.scheduleEvent(iun, recIndex, dateToSchedule, actionType, timelineEventId, null);

        Mockito.verify(actionsPool).addOnlyAction(Mockito.argThat(action ->
                iun.equals(action.getIun()) &&
                        recIndex.equals(action.getRecipientIndex()) &&
                        dateToSchedule.equals(action.getNotBefore()) &&
                        actionType.equals(action.getType()) &&
                        timelineEventId.equals(action.getTimelineId()) &&
                        CommunicationType.INFORMAL == action.getCommunicationType() &&
                        action.getDetails() == null
        ));
    }


}