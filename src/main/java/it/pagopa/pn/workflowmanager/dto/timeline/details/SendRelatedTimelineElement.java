package it.pagopa.pn.workflowmanager.dto.timeline.details;

/**
 * Marker interface for timeline elements that are related to a send operation.
 * Extends RecipientRelatedTimelineElementDetails to indicate that these elements are always associated with a specific recipient.
 */
public interface SendRelatedTimelineElement extends RecipientRelatedTimelineElementDetails {
}
