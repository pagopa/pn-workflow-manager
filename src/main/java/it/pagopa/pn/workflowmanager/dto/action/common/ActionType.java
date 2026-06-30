package it.pagopa.pn.workflowmanager.dto.action.common;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;
import lombok.Getter;

@Getter
public enum ActionType {
  POST_ACCEPTED_PROCESSING_COMPLETED(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("%s_post_accepted_processing",
              action.getIun());
    }
  },
  END_WORKFLOW(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format(
              "%s_end_workflow_recIndex_%d",
              action.getIun(),
              action.getRecipientIndex()
      );
    }
  },
  WORKFLOW_DONE(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format(
              "%s_workflow_done_recIndex_%d",
              action.getIun(),
              action.getRecipientIndex()
      );
    }
  },
  START_WORKFLOW(StartWorkflowDetails.class) {
    @Override
    public String buildActionId(Action action) {
      StartWorkflowDetails details = (StartWorkflowDetails) action.getDetails();
      return String.format(
              "%s_start_workflow_recIndex_%d_stepIndex_%d_channel_%s",
              action.getIun(),
              action.getRecipientIndex(),
              details.getStepIdx(),
              details.getChannel()
      );
    }
  },
  TIMEOUT_WORKFLOW(TimeoutWorkflowDetails.class) {
    @Override
    public String buildActionId(Action action) {
      TimeoutWorkflowDetails details = (TimeoutWorkflowDetails) action.getDetails();
      return String.format(
              "%s_timeout_workflow_recIndex_%d_stepIndex_%d_channel_%s",
              action.getIun(),
              action.getRecipientIndex(),
              details.getStepIdx(),
              details.getChannel()
      );
    }
  };

  private final Class<? extends ActionDetails> detailsJavaClass;

  ActionType(Class<? extends ActionDetails> detailsJavaClass) {
    this.detailsJavaClass = detailsJavaClass;
  }

  public String buildActionId(Action action) {
    throw new UnsupportedOperationException("Must be implemented for each action type");
  }

}
