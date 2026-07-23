package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.PagoPaInt;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPaymentUtilsTest {
    private final String targetNoticeCode = "NOTICE-12345";
    private final String creditorTaxId = "77777777777";
    private final String iun = "IUN-TEST-ABC";
    private final int recIndex = 0;

    @Test
    void getPagoPaPaymentFromNoticeCodeSuccess() {
        NotificationPaymentInfoInt payment1 = NotificationPaymentInfoInt.builder()
                .pagoPA(PagoPaInt.builder()
                        .noticeCode("OTHER-CODE")
                        .amount(1000)
                        .creditorTaxId(creditorTaxId)
                        .build())
                .build();

        NotificationPaymentInfoInt payment2 = NotificationPaymentInfoInt.builder()
                .pagoPA(PagoPaInt.builder()
                        .noticeCode(targetNoticeCode)
                        .creditorTaxId(creditorTaxId)
                        .amount(25000) // 250,00 €
                        .build())
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .payments(List.of(payment1, payment2))
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .recipients(List.of(recipient))
                .build();

        // Act
        PagoPaInt pagoPaInt = NotificationPaymentUtils.getPagoPaPaymentFromNoticeCode(
                notification,
                recIndex,
                targetNoticeCode
        );

        // Assert
        assertEquals(25000, pagoPaInt.getAmount());
        assertEquals(creditorTaxId, pagoPaInt.getCreditorTaxId());
        assertEquals(targetNoticeCode, pagoPaInt.getNoticeCode());
    }

    @Test
    void getPagoPaPaymentFromNoticeCodeWithNullPagoPaDetails() {
        // Arrange - Un pagamento ha il blocco pagoPA a null, l'altro è valido
        NotificationPaymentInfoInt paymentNullPagoPa = NotificationPaymentInfoInt.builder()
                .pagoPA(null)
                .build();

        NotificationPaymentInfoInt paymentValid = NotificationPaymentInfoInt.builder()
                .pagoPA(PagoPaInt.builder()
                        .noticeCode(targetNoticeCode)
                        .creditorTaxId(creditorTaxId)
                        .amount(5000)
                        .build())
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .payments(List.of(paymentNullPagoPa, paymentValid))
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .recipients(List.of(recipient))
                .build();

        // Act
        PagoPaInt pagoPaInt = NotificationPaymentUtils.getPagoPaPaymentFromNoticeCode(
                notification,
                recIndex,
                targetNoticeCode
        );

        // Assert
        assertEquals(5000, pagoPaInt.getAmount());
        assertEquals(creditorTaxId, pagoPaInt.getCreditorTaxId());
        assertEquals(targetNoticeCode, pagoPaInt.getNoticeCode());
    }

    @Test
    void getPagoPaPaymentFromNoticeCodeNotFoundThrowsException() {
        // Arrange - Lista dei pagamenti vuota per il destinatario
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .payments(Collections.emptyList())
                .build();

        NotificationInt notification = NotificationInt.builder()
                .iun(iun)
                .recipients(List.of(recipient))
                .build();

        // Act & Assert
        assertThrows(PnInternalException.class, () -> NotificationPaymentUtils.getPagoPaPaymentFromNoticeCode(
                notification,
                recIndex,
                "NON-EXISTENT-CODE"
        ));
    }
}