package it.pagopa.pn.workflowmanager.dto.ext.delivery.notification;

import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationHistoryResponseInt {
    private final NotificationStatusInt notificationStatus;
    private final List<NotificationStatusHistoryElementInt> notificationStatusHistory;
    private final List<TimelineElementInternal> timeline;
}