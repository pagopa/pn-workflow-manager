package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationStatusHistoryElementInt {
    private final NotificationStatusInt status;
    private final Instant activeFrom;
    private final List<String> relatedTimelineElements;
}
