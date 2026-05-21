package it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto;

import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.details.NotHandledDetails;
import lombok.Getter;

@Getter
public enum ActionType {
  POST_ACCEPTED_PROCESSING_COMPLETED(NotHandledDetails.class) {
    @Override
    public String buildActionId(Action action) {
      return String.format("%s_post_accepted_processing",
              action.getIun());
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
