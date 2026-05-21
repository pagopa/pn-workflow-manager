package it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.dto.details.NotHandledDetails;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "actionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotHandledDetails.class, name = "POST_ACCEPTED_PROCESSING_COMPLETED"),
})
public interface ActionDetails {

}
