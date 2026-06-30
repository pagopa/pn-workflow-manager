package it.pagopa.pn.workflowmanager.dto.timeline.details;

import lombok.Getter;

@Getter
public enum TimelineElementCategoryInt {
    //Timeline Element for Informal Notification
    SEND_DIGITAL_MESSAGE(SendDigitalMessageDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_DIGITAL_MESSAGE_SKIP(SendDigitalMessageSkipDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_DIGITAL_MESSAGE_PROGRESS(SendDigitalMessageProgressDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_DIGITAL_MESSAGE_FEEDBACK(SendDigitalMessageFeedbackDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    PREPARE_ANALOG_DELIVERY(PrepareAnalogDeliveryDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_ANALOG_MESSAGE(SendAnalogMessageDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_ANALOG_MESSAGE_PROGRESS(SendAnalogMessageProgressDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    SEND_ANALOG_MESSAGE_FEEDBACK(SendAnalogMessageFeedbackDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    REACHED(ReachedDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    WORKFLOW_ENDED_REACHED(WorkflowEndedReachedDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    WORKFLOW_ENDED_UNREACHED(WorkflowEndedUnreachedDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    WORKFLOW_ENDED_UNDELIVERABLE(WorkflowEndedUndeliverableDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    WORKFLOW_DONE_REACHED(WorkflowDoneReachedDetailsInt.class, TimelineElementCategoryInt.VERSION_10),
    WORKFLOW_DONE_UNREACHED(WorkflowDoneUnreachedDetailsInt.class, TimelineElementCategoryInt.VERSION_10),    INFORMAL_NOTIFICATION_VIEWED(InformalNotificationViewedDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    COVERPAGE_CREATION_REQUEST(CoverpageCreationRequestDetailsInt.class, TimelineElementCategoryInt.VERSION_10);


    private final Class<? extends TimelineElementDetailsInt> detailsJavaClass;
    private final int priority;
    private final int version;

    public static final int PRIORITY_BEFORE = 10;

    public static final int VERSION_10 = 10;


    TimelineElementCategoryInt(Class<? extends TimelineElementDetailsInt> detailsJavaClass, int version) {
        this(detailsJavaClass, PRIORITY_BEFORE, version);
    }

    TimelineElementCategoryInt(Class<? extends TimelineElementDetailsInt> detailsJavaClass, int priority, int version) {
        this.detailsJavaClass = detailsJavaClass;
        this.priority = priority;
        this.version = version;
    }

    /**
     * Checks if the given category is a known TimelineElementCategoryInt.
     *
     * @param category the category to check
     * @return true if the category is known, false otherwise
     */
    public static boolean isKnownCategory(String category) {
        try {
            TimelineElementCategoryInt.valueOf(category);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
