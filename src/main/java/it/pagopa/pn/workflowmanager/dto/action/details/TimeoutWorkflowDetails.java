package it.pagopa.pn.workflowmanager.dto.action.details;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TimeoutWorkflowDetails implements ActionDetails {
    private int stepIdx;
    private ChannelType channel;
}

