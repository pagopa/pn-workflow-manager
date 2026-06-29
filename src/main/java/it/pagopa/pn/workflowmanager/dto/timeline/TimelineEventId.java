package it.pagopa.pn.workflowmanager.dto.timeline;

import lombok.Getter;

@Getter
public enum TimelineEventId {

    //Builder for event of timeline Element for Informal Notification
    SEND_DIGITAL_MESSAGE("SEND_DIGITAL_MESSAGE"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .build();
        }
    },
    SEND_DIGITAL_MESSAGE_SKIP("SEND_DIGITAL_MESSAGE_SKIP"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .build();
        }
    },
    SEND_DIGITAL_MESSAGE_PROGRESS("SEND_DIGITAL_MESSAGE_PROGRESS"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .withProgressIndex(eventId.getProgressIndex())
                    .build();
        }
    },
    SEND_DIGITAL_MESSAGE_FEEDBACK("SEND_DIGITAL_MESSAGE_FEEDBACK"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .build();
        }
    },
    PREPARE_ANALOG_DELIVERY("PREPARE_ANALOG_DELIVERY"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withSentAttemptMade(eventId.getSentAttemptMade())
                    .withDeliveryType(eventId.getDeliveryType())
                    .build();
        }
    },
    SEND_ANALOG_MESSAGE("SEND_ANALOG_MESSAGE"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withSentAttemptMade(eventId.getSentAttemptMade())
                    .withDeliveryType(eventId.getDeliveryType())
                    .build();
        }
    },
    SEND_ANALOG_MESSAGE_PROGRESS("SEND_ANALOG_MESSAGE_PROGRESS"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withSentAttemptMade(eventId.getSentAttemptMade())
                    .withProgressIndex(eventId.getProgressIndex())
                    .withDeliveryType(eventId.getDeliveryType())
                    .build();
        }
    },
    SEND_ANALOG_MESSAGE_FEEDBACK("SEND_ANALOG_MESSAGE_FEEDBACK"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withSentAttemptMade(eventId.getSentAttemptMade())
                    .withDeliveryType(eventId.getDeliveryType())
                    .build();
        }
    },
    REACHED("REACHED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .build();
        }
    },
    WORKFLOW_ENDED_REACHED("WORKFLOW_ENDED_REACHED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },
    WORKFLOW_ENDED_UNREACHED("WORKFLOW_ENDED_UNREACHED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },
    WORKFLOW_ENDED_UNDELIVERABLE("WORKFLOW_ENDED_UNDELIVERABLE"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },
    WORKFLOW_DONE_REACHED("WORKFLOW_DONE_REACHED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },
    WORKFLOW_DONE_UNREACHED("WORKFLOW_DONE_UNREACHED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },
    INFORMAL_NOTIFICATION_VIEWED("INFORMAL_NOTIFICATION_VIEWED"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .withChannel(eventId.getChannel())
                    .build();
        }
    },
    COVERPAGE_CREATION_REQUEST("COVERPAGE_CREATION_REQUEST"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    };



    public String buildEventId(EventId eventId) {
        throw new UnsupportedOperationException("Must be implemented for each action type event ID");
    }

    public String buildEventId(String eventId) {
        throw new UnsupportedOperationException("Must be implemented for each action type");
    }

    private final String value;

    TimelineEventId(String value) {
        this.value = value;
    }

}