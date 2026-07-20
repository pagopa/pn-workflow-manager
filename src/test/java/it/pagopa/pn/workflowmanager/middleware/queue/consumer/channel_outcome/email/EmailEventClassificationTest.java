package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EmailEventClassificationTest {
    private static Stream<Arguments> emailClassificationCases() {
        return Stream.of(
                Arguments.of(EmailEventClassification.M003, false, ChannelOutcomeCategory.progress()),
                Arguments.of(EmailEventClassification.M004, true, ChannelOutcomeCategory.progress()),
                Arguments.of(EmailEventClassification.M005, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(EmailEventClassification.M006, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(EmailEventClassification.M008, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(EmailEventClassification.M009, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(EmailEventClassification.M010, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(EmailEventClassification.M011, false, ChannelOutcomeCategory.negativeFeedback())
        );
    }

    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @MethodSource("emailClassificationCases")
    void shouldMapCorrectBooleanFlags(EmailEventClassification classification, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Assert
        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {
            "M005", "M006", "M008", "M009", "M010", "M011"
    })
    void shouldReturnEmptyOptionalWhenDesiredFeedbackIsNull(String enumName) {
        // Act
        EmailEventClassification classification = EmailEventClassification.valueOf(enumName);

        // Assert
        assertFalse(classification.getSatisfiedDesiredFeedback().isPresent());
    }


    @ParameterizedTest(name = "Tipo {0} -> desiredFeedback={1}")
    @CsvSource({
            "M003, SENT",
            "M004, RECEIVED"
    })
    void shouldReturnCorrectDesiredFeedbackWhenPresent(EmailEventClassification classification, DesiredFeedbackType expectedFeedback) {
        // Act & Assert
        assertEquals(Optional.of(expectedFeedback), classification.getSatisfiedDesiredFeedback());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"M003", "M004", "M011"})
    void shouldResolveEnumFromExactEventCode(String input) {
        // Act
        EmailEventClassification result = EmailEventClassification.fromEventCode(input);

        // Assert
        assertEquals(EmailEventClassification.valueOf(input), result);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"m003", "M003 ", "invalid_code", ""})
    void shouldThrowExceptionWhenCodeIsUnknownOrNotExactMatch(String invalidCode) {
        // Act & Assert
        // NB: come PecEventClassification, il matching usa direttamente valueOf()
        // ed è quindi case-sensitive e senza trim
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> EmailEventClassification.fromEventCode(invalidCode)
        );
    }

    @Test
    void shouldThrowExceptionWhenCodeIsCompletelyUnknown() {
        // Arrange
        String unknownType = "INVALID_EVENT_CODE";

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> EmailEventClassification.fromEventCode(unknownType)
        );
    }
}