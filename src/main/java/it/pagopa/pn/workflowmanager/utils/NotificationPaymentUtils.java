package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_PAYMENT_NOT_FOUND;

public class NotificationPaymentUtils {
    public static int getAmountFromNotificationPagoPaPayment(NotificationInt notification, int recIndex, String noticeCode) {
        NotificationRecipientInt recipient = notification.getRecipients().get(recIndex);
        return recipient.getPayments().stream()
                .filter(payment -> payment.getPagoPA() != null && noticeCode.equals(payment.getPagoPA().getNoticeCode()))
                .mapToInt(payment -> payment.getPagoPA().getAmount())
                .findFirst()
                .orElseThrow(() -> new PnInternalException(
                    String.format(
                        "No payment found for noticeCode: %s in notification with iun: %s and recipient index: %d",
                        noticeCode,
                        notification.getIun(),
                        recIndex
                    ),
                    ERROR_CODE_WORKFLOWMANAGER_PAYMENT_NOT_FOUND
                ));
    }
}
