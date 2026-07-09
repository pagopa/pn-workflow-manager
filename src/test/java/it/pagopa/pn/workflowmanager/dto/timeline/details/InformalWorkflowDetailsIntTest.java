package it.pagopa.pn.workflowmanager.dto.timeline.details;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class InformalWorkflowDetailsIntTest {

    @Test
    void coverpageCreationRequestToLog() {
        CoverpageCreationRequestDetailsInt details = CoverpageCreationRequestDetailsInt.builder()
                .recIndex(1)
                .fileKey("fileKey")
                .build();

        Assertions.assertEquals("recIndex=1 fileKey=fileKey", details.toLog());
        Assertions.assertEquals("fileKey", details.getFileKey());
    }

    @Test
    void reachedToLog() {
        DeliveredDetailsInt details = DeliveredDetailsInt.builder()
                .recIndex(2)
                .channel("SMS")
                .sourceElementId("sourceElementId")
                .notificationDate(Instant.parse("2024-03-01T08:00:00Z"))
                .build();

        Assertions.assertEquals("recIndex=2 channel=SMS sourceElementId=sourceElementId notificationDate=2024-03-01T08:00:00Z", details.toLog());
    }

    @Test
    void informalNotificationViewedToLogAndTimestamp() {
        Instant eventTimestamp = Instant.parse("2024-03-01T08:00:00Z");
        InformalNotificationViewedDetailsInt details = InformalNotificationViewedDetailsInt.builder()
                .recIndex(3)
                .sourceChannel("APPIO")
                .sourceChannelDetails("push")
                .eventTimestamp(eventTimestamp)
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d sourceChannel=%s sourceChannelDetails=%s eventTimestamp=%s",
                        3, "APPIO", "push", eventTimestamp),
                details.toLog()
        );
    }

    @Test
    void workflowDoneReachedToLog() {
        WorkflowDoneReachedDetailsInt details = WorkflowDoneReachedDetailsInt.builder()
                .recIndex(4)
                .sourceElementId("sourceElementId")
                .completionFeedback("SENT")
                .build();

        Assertions.assertEquals("recIndex=4 sourceElementId=sourceElementId completionFeedback=SENT", details.toLog());
    }

    @Test
    void workflowDoneUnreachedToLog() {
        WorkflowDoneUnreachedDetailsInt details = WorkflowDoneUnreachedDetailsInt.builder()
                .recIndex(4)
                .sourceElementId("sourceElementId")
                .completionFeedback("SENT")
                .build();

        Assertions.assertEquals("recIndex=4 sourceElementId=sourceElementId completionFeedback=SENT", details.toLog());
    }

    @Test
    void workflowEndedReachedToLogAndTimestamp() {
        WorkflowEndedReachedDetailsInt details = WorkflowEndedReachedDetailsInt.builder()
                .recIndex(5)
                .sourceElementId("elementId")
                .build();

        Assertions.assertEquals(
                String.format("recIndex=%d sourceElementId=%s",
                        5, "elementId"),
                details.toLog()
        );
    }

    @Test
    void workflowEndedUndeliverableToLog() {
        WorkflowEndedUndeliverableDetailsInt details = WorkflowEndedUndeliverableDetailsInt.builder()
                .recIndex(6)
                .build();

        Assertions.assertEquals("recIndex=6", details.toLog());
    }

    @Test
    void workflowEndedUnreachedToLog() {
        WorkflowEndedUnreachedDetailsInt details = WorkflowEndedUnreachedDetailsInt.builder()
                .recIndex(7)
                .sourceElementId("elementId")
                .build();

        Assertions.assertEquals("recIndex=7 sourceElementId=elementId", details.toLog());
    }
}
