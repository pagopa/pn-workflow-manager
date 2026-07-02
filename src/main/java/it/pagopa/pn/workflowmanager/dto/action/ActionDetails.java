package it.pagopa.pn.workflowmanager.dto.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.TimeoutWorkflowDetails;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "POST_ACCEPTED_PROCESSING_COMPLETED"),
        @JsonSubTypes.Type(value = StartWorkflowDetails.class, name = "START_WORKFLOW"),
        @JsonSubTypes.Type(value = TimeoutWorkflowDetails.class, name = "TIMEOUT_WORKFLOW"),
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "END_WORKFLOW"),
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "WORKFLOW_DONE"),
        @JsonSubTypes.Type(value = DocumentCreationResponseActionDetails.class, name = "DOCUMENT_CREATION_RESPONSE")
})
public interface ActionDetails {

}
