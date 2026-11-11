package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.api.dto.events.PnNoticePaymentPaidEvent;
import it.pagopa.pn.workflowmanager.dto.event.NotificationPaidInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import it.pagopa.pn.workflowmanager.dto.timeline.details.NoticePaymentInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoticePaymentMapperTest {

    @Test
    void messageToInternalCopiesPaymentEventFields() {
        Instant paymentDateTime = Instant.parse("2026-10-28T12:00:00Z");
        LocalDate dueDate = LocalDate.parse("2026-11-30");
        Instant persistedTimestamp = Instant.parse("2026-10-28T12:01:00Z");
        PnNoticePaymentPaidEvent event = PnNoticePaymentPaidEvent.builder()
                .payload(PnNoticePaymentPaidEvent.Payload.builder()
                        .modelType("MODEL")
                        .iuv("IUV-001")
                        .iur("IUR-001")
                        .noticeCode("NOTICE-001")
                        .creditorTaxId("TAX-ID")
                        .creditorCompanyName("Company")
                        .paymentDateTime(paymentDateTime)
                        .dueDate(dueDate)
                        .totalNotice(1300)
                        .paymentSourceChannel("PPA")
                        .amount(1200)
                        .paymentMethod("CARD")
                        .fee("100")
                        .paymentId("PAYMENT-001")
                        .pagopaPersistedTimestamp(persistedTimestamp)
                        .pagopaPublishedTimestamp(persistedTimestamp.plusSeconds(1))
                        .sendIngestionTimestamp(persistedTimestamp.plusSeconds(2))
                        .kafkaPartition("partition-1")
                        .iun("IUN-001")
                        .recIndex(0)
                        .communicationType("INFORMAL")
                        .build())
                .build();

        NoticePaymentInt actual = NoticePaymentMapper.messageToInternal(event);

        assertEquals("MODEL", actual.getModelType());
        assertEquals("IUV-001", actual.getIuv());
        assertEquals("IUR-001", actual.getIur());
        assertEquals("NOTICE-001", actual.getNoticeCode());
        assertEquals("TAX-ID", actual.getCreditorTaxId());
        assertEquals("Company", actual.getCreditorCompanyName());
        assertEquals(paymentDateTime, actual.getPaymentDateTime());
        assertEquals(dueDate, actual.getDueDate());
        assertEquals(1300, actual.getTotalNotice());
        assertEquals("PPA", actual.getPaymentSourceChannel());
        assertEquals(1200, actual.getAmount());
        assertEquals("CARD", actual.getPaymentMethod());
        assertEquals("100", actual.getFee());
        assertEquals("PAYMENT-001", actual.getPaymentId());
        assertEquals(persistedTimestamp, actual.getPagopaPersistedTimestamp());
        assertEquals(persistedTimestamp.plusSeconds(1), actual.getPagopaPublishedTimestamp());
        assertEquals(persistedTimestamp.plusSeconds(2), actual.getSendIngestionTimestamp());
        assertEquals("partition-1", actual.getKafkaPartition());
        assertEquals("IUN-001", actual.getIun());
        assertEquals(0, actual.getRecIndex());
        assertEquals(CommunicationType.INFORMAL, actual.getCommunicationType());
    }

    @Test
    void mapToNotificationPaidIntCopiesFieldsUsedByPaymentTimeline() {
        Instant paymentDateTime = Instant.parse("2026-10-28T12:00:00Z");
        NoticePaymentInt event = NoticePaymentInt.builder()
                .iun("IUN-001")
                .recIndex(0)
                .noticeCode("NOTICE-001")
                .creditorTaxId("TAX-ID")
                .amount(1200)
                .paymentSourceChannel("PPA")
                .paymentDateTime(paymentDateTime)
                .build();

        NotificationPaidInt actual = NoticePaymentMapper.mapToNotificationPaidInt(event);

        assertEquals("IUN-001", actual.getIun());
        assertEquals(0, actual.getRecIndex());
        assertEquals("NOTICE-001", actual.getNoticeCode());
        assertEquals("TAX-ID", actual.getCreditorTaxId());
        assertEquals(1200, actual.getAmount());
        assertEquals("PPA", actual.getPaymentSourceChannel());
        assertEquals(paymentDateTime, actual.getEventTimestamp());
    }
}
