package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SmsEventClassificationTest {

    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @CsvSource({
            "S003, false, FEEDBACK",
            "S008, false, FEEDBACK",
            "S010, false, FEEDBACK"
    })
    void shouldMapCorrectBooleanFlags(String enumName, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Act
        SmsEventClassification classification = SmsEventClassification.valueOf(enumName);

        // Assert
        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"S008", "S010"})
    void shouldReturnEmptyOptionalWhenDesiredFeedbackIsNull(String enumName) {
        // Act
        SmsEventClassification classification = SmsEventClassification.valueOf(enumName);

        // Assert
        assertFalse(classification.getSatisfiedDesiredFeedback().isPresent());
    }

    @Test
    void shouldReturnCorrectDesiredFeedbackWhenPresent() {
        // Act & Assert
        assertEquals(Optional.of(DesiredFeedbackType.SENT), SmsEventClassification.S003.getSatisfiedDesiredFeedback());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"S003", "S008", "S010"})
    void shouldResolveEnumFromExactEventCode(String input) {
        // Act
        SmsEventClassification result = SmsEventClassification.fromEventCode(input);

        // Assert
        assertEquals(SmsEventClassification.valueOf(input), result);
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"s003", "S003 ", "invalid_code", ""})
    void shouldThrowExceptionWhenCodeIsUnknownOrNotExactMatch(String invalidCode) {
        // Act & Assert
        // NB: come PecEventClassification/EmailEventClassification, il matching usa
        // direttamente valueOf() ed è quindi case-sensitive e senza trim
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> SmsEventClassification.fromEventCode(invalidCode)
        );
    }

    @Test
    void shouldThrowExceptionWhenCodeIsCompletelyUnknown() {
        // Arrange
        String unknownType = "INVALID_EVENT_CODE";

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> SmsEventClassification.fromEventCode(unknownType)
        );
    }

    @Test
    void shouldReturnTrueForSuccessEventOnlyOnS003() {
        // Act & Assert
        assertTrue(SmsEventClassification.S003.isSuccessEvent());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"S008", "S010"})
    void shouldReturnFalseForSuccessEventOnOtherCodes(String enumName) {
        // Act
        SmsEventClassification classification = SmsEventClassification.valueOf(enumName);

        // Assert
        assertFalse(classification.isSuccessEvent());
    }
}