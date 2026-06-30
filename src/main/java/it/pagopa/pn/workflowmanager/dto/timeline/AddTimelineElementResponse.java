package it.pagopa.pn.workflowmanager.dto.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class AddTimelineElementResponse {
    private String timelineElementId;
    private boolean isDuplicate;
}
