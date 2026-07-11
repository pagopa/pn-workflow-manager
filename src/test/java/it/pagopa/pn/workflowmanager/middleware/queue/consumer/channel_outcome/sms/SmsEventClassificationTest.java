package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SmsEventClassificationTest {

    private static Stream<Arguments> smsClassificationCases() {
        return Stream.of(
                Arguments.of(SmsEventClassification.S003, false, ChannelOutcomeCategory.positiveFeedback(false)),
                Arguments.of(SmsEventClassification.S008, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(SmsEventClassification.S010, false, ChannelOutcomeCategory.negativeFeedback())
        );
    }

    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @MethodSource("smsClassificationCases")
    void shouldMapCorrectBooleanFlags(SmsEventClassification classification, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
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
}