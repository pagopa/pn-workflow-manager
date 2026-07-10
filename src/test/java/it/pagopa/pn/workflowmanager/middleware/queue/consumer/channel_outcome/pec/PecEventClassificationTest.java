package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PecEventClassificationTest {

    private static Stream<Arguments> pecClassificationCases() {
        return Stream.of(
                Arguments.of(PecEventClassification.C000, false, ChannelOutcomeCategory.progress()),
                Arguments.of(PecEventClassification.C001, false, ChannelOutcomeCategory.progress()),
                Arguments.of(PecEventClassification.C002, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C003, true, ChannelOutcomeCategory.positiveFeedback()),
                Arguments.of(PecEventClassification.C004, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C005, false, ChannelOutcomeCategory.progress()),
                Arguments.of(PecEventClassification.C006, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C007, false, ChannelOutcomeCategory.progress()),
                Arguments.of(PecEventClassification.C008, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C009, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C010, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(PecEventClassification.C011, false, ChannelOutcomeCategory.negativeFeedback())
        );
    }

    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @MethodSource("pecClassificationCases")
    void shouldMapCorrectBooleanFlags(PecEventClassification classification, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Assert
        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {
            "C000", "C002", "C004", "C005", "C006",
            "C007", "C008", "C009", "C010", "C011"
    })
    void shouldReturnEmptyOptionalWhenDesiredFeedbackIsNull(String enumName) {
        // Act
        PecEventClassification classification = PecEventClassification.valueOf(enumName);

        // Assert
        assertFalse(classification.getSatisfiedDesiredFeedback().isPresent());
    }

    @ParameterizedTest(name = "Tipo {0} -> desiredFeedback={1}")
    @CsvSource({
            "C001, SENT",
            "C003, RECEIVED"
    })
    void shouldReturnCorrectDesiredFeedbackWhenPresent(PecEventClassification classification, DesiredFeedbackType expectedFeedback) {
        // Act & Assert
        assertEquals(Optional.of(expectedFeedback), classification.getSatisfiedDesiredFeedback());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"C000", "C003", "C011"})
    void shouldResolveEnumFromExactEventCode(String input) {
        // Act
        PecEventClassification result = PecEventClassification.fromEventCode(input);

        // Assert
        assertEquals(PecEventClassification.valueOf(input), result);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"c003", "C003 ", "invalid_code", ""})
    void shouldThrowExceptionWhenCodeIsUnknownOrNotExactMatch(String invalidCode) {
        // Act & Assert
        // NB: a differenza di IoEventClassification.fromEventType, qui il matching
        // usa direttamente valueOf() ed è quindi case-sensitive e senza trim
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> PecEventClassification.fromEventCode(invalidCode)
        );
    }

    @Test
    void shouldThrowExceptionWhenCodeIsCompletelyUnknown() {
        // Arrange
        String unknownType = "INVALID_EVENT_CODE";

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> PecEventClassification.fromEventCode(unknownType)
        );
    }
}