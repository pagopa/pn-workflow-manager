package it.pagopa.pn.workflowmanager.dto.action.details;

import it.pagopa.pn.workflowmanager.dto.action.ActionDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.Channel;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StartWorkflowDetails implements ActionDetails {
    private int stepIdx;
    private Channel channel;
}
