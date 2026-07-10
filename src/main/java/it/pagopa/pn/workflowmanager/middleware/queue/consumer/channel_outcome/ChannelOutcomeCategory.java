package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

public sealed interface ChannelOutcomeCategory permits ChannelOutcomeCategory.Progress, ChannelOutcomeCategory.Feedback {

    record Progress() implements ChannelOutcomeCategory {}

    record Feedback(FeedbackOutcome outcome) implements ChannelOutcomeCategory {}

    default boolean isNegativeFeedback() {
        return this instanceof Feedback(FeedbackOutcome outcome) && outcome == FeedbackOutcome.NEGATIVE;
    }

    enum FeedbackOutcome { POSITIVE, NEGATIVE }

    static ChannelOutcomeCategory.Progress progress() {
        return new Progress();
    }

    static ChannelOutcomeCategory.Feedback positiveFeedback() {
        return new Feedback(FeedbackOutcome.POSITIVE);
    }

    static ChannelOutcomeCategory.Feedback negativeFeedback() {
        return new Feedback(FeedbackOutcome.NEGATIVE);
    }
}
