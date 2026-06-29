package it.pagopa.pn.workflowmanager.dto.timeline.details.common;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder( toBuilder = true )
@ToString
public class CategoryTypeTimelineElementDetailsInt {
    protected String categoryType;
}
