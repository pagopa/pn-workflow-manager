package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PecEventClassificationTest {

        @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
        @CsvSource({
                "C000, false, PROGRESS",
                "C001, false, PROGRESS",
                "C002, false, FEEDBACK",
                "C003, true,  FEEDBACK",
                "C004, false, FEEDBACK",
                "C005, false, PROGRESS",
                "C006, false, FEEDBACK",
                "C007, false, PROGRESS",
                "C008, false, FEEDBACK",
                "C009, false, FEEDBACK",
                "C010, false, FEEDBACK",
                "C011, false, FEEDBACK"
        })
        void shouldMapCorrectBooleanFlags(String enumName, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
            // Act
            PecEventClassification classification = PecEventClassification.valueOf(enumName);

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

        @Test
        void shouldReturnTrueForSuccessfulDeliveryOnlyOnC003() {
            // Act & Assert
            assertTrue(PecEventClassification.C003.isSuccessfulDelivery());
        }

        @ParameterizedTest(name = "Input: {0}")
        @ValueSource(strings = {
                "C000", "C001", "C002", "C004", "C005",
                "C006", "C007", "C008", "C009", "C010", "C011"
        })
        void shouldReturnFalseForSuccessfulDeliveryOnOtherCodes(String enumName) {
            // Act
            PecEventClassification classification = PecEventClassification.valueOf(enumName);

            // Assert
            assertFalse(classification.isSuccessfulDelivery());
        }
}