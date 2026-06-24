package it.pagopa.pn.workflowmanager.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NewTimelineElement;
import it.pagopa.pn.workflowmanager.dto.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TimelineServiceMapperTest {
    //ToDo: aggiustare alcuni metodi di test in questa classe
    private final TimelineServiceMapper timelineServiceMapper;

    public TimelineServiceMapperTest() {
        SmartMapper smartMapper = new SmartMapper(new ObjectMapper());
        this.timelineServiceMapper = new TimelineServiceMapper(smartMapper);
    }

//    @Test
//    void toTimelineElementDetailsInt() {
//        NotificationViewedDetails details = new NotificationViewedDetails()
//                .categoryType("NOTIFICATION_VIEWED")
//                .recIndex(5)
//                .eventTimestamp(Instant.now())
//                .raddType("FSU")
//                .raddTransactionId("RADD123")
//                .notificationCost(1000L);
//
//        TimelineElementDetailsInt result = timelineServiceMapper.toTimelineElementDetailsInt(details, TimelineElementCategoryInt.NOTIFICATION_VIEWED);
//        InformalNotificationViewedDetailsInt notificationViewedDetailsInt = (InformalNotificationViewedDetailsInt) result;
//
//        assertNotNull(notificationViewedDetailsInt);
//        assertEquals(details.getRecIndex(), notificationViewedDetailsInt.getRecIndex());
//        assertEquals(details.getNotificationCost().intValue(), notificationViewedDetailsInt.getNotificationCost());
//    }

//    @Test
//    void toTimelineElementDetailsIntCategory() {
//        TimelineElement timelineElement = new TimelineElement()
//                .iun("IUN12345")
//                .elementId("ELEM001")
//                .timestamp(Instant.now())
//                .paId("PA_TEST")
//                .category(TimelineCategory.INFORMAL_NOTIFICATION_VIEWED)
//                .details(new InformalNotificationViewedDetails().categoryType("TEST").recIndex(0))
//                .statusInfo(new StatusInfo().actual("REACHED").statusChangeTimestamp(Instant.now()).statusChanged(true))
//                .notificationSentAt(Instant.now())
//                .ingestionTimestamp(Instant.now())
//                .eventTimestamp(Instant.now());
//
//        TimelineElementCategoryInt category = TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED;
//        TimelineElementDetailsInt result = timelineServiceMapper.toTimelineElementDetailsInt(timelineElement.getDetails(), category);
//        assertNotNull(result);
//    }

    @Test
    void getNewTimelineElement_mapsFieldsCorrectly() {
        // Arrange
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN_TEST")
                .elementId("ELEM_ID")
                .category(TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED)
                .build();

        NotificationRecipientInt recipient1 = NotificationRecipientInt.builder().internalId("rec1").build();
        NotificationRecipientInt recipient2 = NotificationRecipientInt.builder().internalId("rec2").build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN_TEST")
                .paProtocolNumber("PROT_123")
                .sentAt(Instant.now())
                .recipients(List.of(recipient1, recipient2))
                .build();

        // Act
        NewTimelineElement result = timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTimelineElement());
        assertNotNull(result.getNotificationInfo());
        assertEquals("IUN_TEST", result.getTimelineElement().getIun());
        assertEquals("PROT_123", result.getNotificationInfo().getPaProtocolNumber());
        assertEquals(2, result.getNotificationInfo().getNumberOfRecipients());
    }

    @Test
    void getNewTimelineElement_mapsFieldsCorrectly_withLegalFacts() {
        // Arrange
        TimelineElementInternal timelineElementInternal = TimelineElementInternal.builder()
                .iun("IUN_TEST")
                .elementId("ELEM_ID")
                .category(TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED)
                .build();

        NotificationRecipientInt recipient1 = NotificationRecipientInt.builder().internalId("rec1").build();
        NotificationRecipientInt recipient2 = NotificationRecipientInt.builder().internalId("rec2").build();

        NotificationInt notificationInt = NotificationInt.builder()
                .iun("IUN_TEST")
                .paProtocolNumber("PROT_123")
                .sentAt(Instant.now())
                .recipients(List.of(recipient1, recipient2))
                .build();

        // Act
        NewTimelineElement result = timelineServiceMapper.getNewTimelineElement(timelineElementInternal, notificationInt);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTimelineElement());
        assertNotNull(result.getNotificationInfo());
        assertEquals("IUN_TEST", result.getTimelineElement().getIun());
        assertEquals("PROT_123", result.getNotificationInfo().getPaProtocolNumber());
        assertEquals(2, result.getNotificationInfo().getNumberOfRecipients());
    }

//    @Test
//    void toTimelineElementDetailsInt_mapsFieldsCorrectly() {
//        TimelineElementDetails details = new CoverpageCreationRequestDetails()
//                .categoryType("COVERPAGE_CREATION_REQUEST")
//                .recIndex(0);
//
//        TimelineElement timelineElement = new TimelineElement()
//                .details(details)
//                .category(TimelineCategory.COVERPAGE_CREATION_REQUEST);
//
//        TimelineElementCategoryInt category = TimelineElementCategoryInt.COVERPAGE_CREATION_REQUEST;
//
//        Object result = timelineServiceMapper.toTimelineElementDetailsInt(timelineElement.getDetails(), category);
//
//        assertNotNull(result);
//        assertInstanceOf(CoverpageCreationRequestDetailsInt.class, result);
//        CoverpageCreationRequestDetailsInt coverpageCreationRequestDetailsInt = (CoverpageCreationRequestDetailsInt) result;
//        assertEquals("COVERPAGE_CREATION_REQUEST", coverpageCreationRequestDetailsInt.getCategoryType());
//        assertEquals(0, coverpageCreationRequestDetailsInt.getRecIndex());
//    }

//    @Test
//    void toTimelineElementInternal_mapsFieldsCorrectly() {
//        TimelineElementDetails details = new InformalNotificationViewedDetails().categoryType("INFORMAL_NOTIFICATION_VIEWED").recIndex(1);
//        StatusInfo statusInfo = new StatusInfo().actual("REACHED").statusChangeTimestamp(Instant.now()).statusChanged(true);
//        TimelineElement timelineElement = new TimelineElement()
//                .iun("IUN_TEST")
//                .elementId("ELEM_ID")
//                .timestamp(Instant.now())
//                .paId("PA_TEST")
//                .category(TimelineCategory.INFORMAL_NOTIFICATION_VIEWED)
//                .details(details)
//                .statusInfo(statusInfo)
//                .notificationSentAt(Instant.now())
//                .ingestionTimestamp(Instant.now())
//                .eventTimestamp(Instant.now());
//
//        TimelineElementInternal result = timelineServiceMapper.toTimelineElementInternal(timelineElement);
//
//        assertNotNull(result);
//        assertEquals("IUN_TEST", result.getIun());
//        assertEquals("ELEM_ID", result.getElementId());
//        assertEquals(TimelineElementCategoryInt.INFORMAL_NOTIFICATION_VIEWED, result.getCategory());
//        assertNotNull(result.getDetails());
//        assertNotNull(result.getStatusInfo());
//    }

}