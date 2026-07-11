package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AnalogEventClassificationTest {

    private static Stream<Arguments> analogClassificationCases() {
        return Stream.of(
                Arguments.of(AnalogEventClassification.PROGRESS, false, ChannelOutcomeCategory.progress()),
                Arguments.of(AnalogEventClassification.OK,       true,  ChannelOutcomeCategory.positiveFeedback()),
                Arguments.of(AnalogEventClassification.KO,       false, ChannelOutcomeCategory.negativeFeedback())
        );
    }

    @ParameterizedTest(name = "{0} -> recipientReached={1}, category={2}")
    @MethodSource("analogClassificationCases")
    void shouldHaveCorrectCategoryAndRecipientReachedFlag(
            AnalogEventClassification classification,
            boolean expectedReached,
            ChannelOutcomeCategory expectedCategory) {

        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @ParameterizedTest(name = "{0} -> desiredFeedback assente")
    @ValueSource(strings = {"KO"})
    void shouldReturnEmptyDesiredFeedbackWhenNotApplicable(String enumName) {
        AnalogEventClassification classification = AnalogEventClassification.valueOf(enumName);

        assertFalse(classification.getSatisfiedDesiredFeedback().isPresent());
    }

    @Test
    void shouldReturnReceivedDesiredFeedbackForOk() {
        assertEquals(Optional.of(DesiredFeedbackType.RECEIVED),
                AnalogEventClassification.OK.getSatisfiedDesiredFeedback());
    }

    @Test
    void shouldReturnSentDesiredFeedbackForProgress() {
        assertEquals(Optional.of(DesiredFeedbackType.SENT),
                AnalogEventClassification.PROGRESS.getSatisfiedDesiredFeedback());
    }


    @ParameterizedTest(name = "fromStatusEventCode(\"{0}\")")
    @ValueSource(strings = {"PROGRESS", "OK", "KO"})
    void shouldResolveClassificationFromValidStatusCode(String code) {
        AnalogEventClassification result = AnalogEventClassification.fromStatusEventCode(code);

        assertEquals(AnalogEventClassification.valueOf(code), result);
    }

    @ParameterizedTest(name = "fromStatusEventCode(\"{0}\") -> eccezione")
    @ValueSource(strings = {"progress", "ok", "ko", "UNKNOWN", ""})
    void shouldThrowExceptionWhenStatusCodeIsInvalid(String invalidCode) {
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> AnalogEventClassification.fromStatusEventCode(invalidCode)
        );
    }
}