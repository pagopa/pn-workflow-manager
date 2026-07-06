package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.action;


import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.action.common.Action;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_REACHED;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED;

@Slf4j
public abstract class AbstractActionEventHandler implements EventHandler<Action> {

    protected final TimelineUtils timelineUtils;

    protected AbstractActionEventHandler(TimelineUtils timelineUtils) {
        this.timelineUtils = timelineUtils;
    }

    protected void checkWorkflowDoneOrExecute(List<TimelineElementInternal> timelineElements,Action action, Consumer<Action> functionToCall) {
        if(!isWorkflowDoneReachedOrUnreached(timelineElements, action.getRecipientIndex())) {
            functionToCall.accept(action);
        } else {
            log.info("Workflow is already DONE, the action will not be executed - iun={}", action.getIun());
        }
    }

    private boolean isWorkflowDoneReachedOrUnreached(List<TimelineElementInternal> timelineElements, int recIndex) {
        return timelineUtils.checkTimelineCategories(timelineElements, recIndex, WORKFLOW_DONE_REACHED, WORKFLOW_DONE_UNREACHED);
    }

    public Class<Action> getPayloadType() {
        return Action.class;
    }
}
