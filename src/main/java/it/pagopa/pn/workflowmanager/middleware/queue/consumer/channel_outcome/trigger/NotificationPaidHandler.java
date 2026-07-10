package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NotificationPaidDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPaidHandler {

    private final NotificationService notificationService;
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;

    public void handlePaymentPaid(NotificationPaidInt payment) {
        NotificationInt notification = notificationService.getInformalNotificationByIun(payment.getIun());
        String timelineId = TimelineEventId.NOTIFICATION_PAID.buildEventId(
                EventId.builder()
                        .iun(payment.getIun())
                        .noticeCode(payment.getNoticeCode())
                        .creditorTaxId(payment.getCreditorTaxId())
                        .build()
        );

        NotificationRecipientInt recipient = notification.getRecipients().get(payment.getRecIndex());

        NotificationPaidDetailsInt details = timelineUtils.buildNotificationPaidDetails(payment, recipient);

        TimelineElementInternal timelineElement = timelineUtils.buildTimeline(
                notification,
                TimelineElementCategoryInt.PAYMENT,
                timelineId,
                details
        );

        log.info("Persisting PAYMENT timeline element for iun={} recIndex={} timelineId={}",
                payment.getIun(), payment.getRecIndex(), timelineId);
        timelineService.addTimelineElement(timelineElement, notification);
    }
}
