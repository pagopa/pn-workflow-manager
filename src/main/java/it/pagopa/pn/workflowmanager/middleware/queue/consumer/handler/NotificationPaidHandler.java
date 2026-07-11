package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
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
        log.info("Handling payment for iun={} recIndex={} noticeCode={} creditorTaxId={}",
                payment.getIun(), payment.getRecIndex(), payment.getNoticeCode(), payment.getCreditorTaxId());
        NotificationInt notification = notificationService.getInformalNotificationByIun(payment.getIun());
        NotificationRecipientInt recipient = notification.getRecipients().get(payment.getRecIndex());

        TimelineElementInternal timelineElement = timelineUtils.buildPaymentTimelineElement(notification, payment, recipient);

        timelineService.addTimelineElement(timelineElement, notification);
    }
}
