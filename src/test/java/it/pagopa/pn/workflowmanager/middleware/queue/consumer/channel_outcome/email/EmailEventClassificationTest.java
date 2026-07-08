package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmailEventClassificationTest {
    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @CsvSource({
            "M003, false, PROGRESS",
            "M004, true,  PROGRESS",
            "M005, false, FEEDBACK",
            "M006, false, FEEDBACK",
            "M008, false, FEEDBACK",
            "M009, false, FEEDBACK",
            "M010, false, FEEDBACK",
            "M011, false, FEEDBACK"
    })
    void shouldMapCorrectBooleanFlags(String enumName, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Act
        EmailEventClassification classification = EmailEventClassification.valueOf(enumName);

        // Assert
        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {
            "M004", "M005", "M006", "M008", "M009", "M010", "M011"
    })
    void shouldReturnEmptyOptionalWhenDesiredFeedbackIsNull(String enumName) {
        // Act
        EmailEventClassification classification = EmailEventClassification.valueOf(enumName);

        // Assert
        assertFalse(classification.getSatisfiedDesiredFeedback().isPresent());
    }

    @Test
    void shouldReturnCorrectDesiredFeedbackWhenPresent() {
        // Act & Assert
        assertEquals(Optional.of(DesiredFeedbackType.SENT), EmailEventClassification.M003.getSatisfiedDesiredFeedback());
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