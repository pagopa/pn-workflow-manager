package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

public sealed interface ChannelOutcomeCategory permits ChannelOutcomeCategory.Progress, ChannelOutcomeCategory.Feedback {

    record Progress() implements ChannelOutcomeCategory {}

    record Feedback(FeedbackOutcome outcome, boolean shouldAdvanceWorkflow) implements ChannelOutcomeCategory {}

    enum FeedbackOutcome { POSITIVE, NEGATIVE }

    /**
     * Creates a progress outcome
     *
     * @return a new instance of Progress
     */
    static ChannelOutcomeCategory.Progress progress() {
        return new Progress();
    }

    /**
     * Creates a positive feedback outcome
     *
     * @param shouldAdvanceWorkflow indicates whether the workflow should advance after this feedback
     * @return a new instance of Feedback with outcome POSITIVE and the specified shouldAdvanceWorkflow value
     */
    static ChannelOutcomeCategory.Feedback positiveFeedback(boolean shouldAdvanceWorkflow) {
        return new Feedback(FeedbackOutcome.POSITIVE, shouldAdvanceWorkflow);
    }

    /**
     * Creates a negative feedback outcome (NEGATIVE Feedbacks should always advance the workflow, so shouldAdvanceWorkflow is set to true)
     *
     * @return a new instance of Feedback with outcome NEGATIVE and shouldAdvanceWorkflow set to true
     */
    static ChannelOutcomeCategory.Feedback negativeFeedback() {
        return new Feedback(FeedbackOutcome.NEGATIVE, true);
    }

    default boolean isNegativeFeedback() {
        return this instanceof Feedback f && f.outcome == FeedbackOutcome.NEGATIVE;
    }

    default boolean shouldAdvanceWorkflow() {
        return this instanceof Feedback f && f.shouldAdvanceWorkflow;
    }
}
