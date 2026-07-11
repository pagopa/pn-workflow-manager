package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPaidHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    @InjectMocks
    private NotificationPaidHandler notificationPaidHandler;

    @Test
    void handlePaymentPaidBuildsAndPersistsPaymentTimelineElement() {
        Instant sentAt = Instant.parse("2026-07-10T09:00:00Z");
        Instant eventTimestamp = Instant.parse("2026-07-10T09:30:00Z");

        NotificationInt notification = NotificationInt.builder()
                .iun("IUN_TEST")
                .sentAt(sentAt)
                .sender(NotificationSenderInt.builder().paId("PA_ID").build())
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .recipientType(RecipientTypeInt.PF)
                                .build()
                ))
                .build();

        NotificationPaidInt payment = NotificationPaidInt.builder()
                .iun("IUN_TEST")
                .recIndex(0)
                .noticeCode("NOTICE_123")
                .creditorTaxId("CREDITOR_456")
                .paymentSourceChannel("IO")
                .amount(100)
                .eventTimestamp(eventTimestamp)
                .build();

        TimelineElementInternal builtTimelineElement = TimelineElementInternal.builder()
                .iun("IUN_TEST")
                .elementId("NOTIFICATION_PAID.IUN_IUN_TEST.CODE_PPANOTICE_123CREDITOR_456")
                .category(TimelineElementCategoryInt.PAYMENT)
                .build();

        when(notificationService.getInformalNotificationByIun("IUN_TEST")).thenReturn(notification);
        when(timelineUtils.buildPaymentTimelineElement(eq(notification), eq(payment), eq(notification.getRecipients().getFirst())))
                .thenReturn(builtTimelineElement);

        notificationPaidHandler.handlePaymentPaid(payment);

        verify(timelineUtils).buildPaymentTimelineElement(eq(notification), eq(payment), eq(notification.getRecipients().getFirst()));
        verify(timelineService).addTimelineElement(eq(builtTimelineElement), eq(notification));
    }
}
