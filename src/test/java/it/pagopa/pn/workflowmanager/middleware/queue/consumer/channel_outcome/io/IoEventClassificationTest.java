package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

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

class IoEventClassificationTest {
    private static Stream<Arguments> ioClassificationCases() {
        return Stream.of(
                Arguments.of(IoEventClassification.DELIVERED_TO_USER, true, ChannelOutcomeCategory.progress()),
                Arguments.of(IoEventClassification.SENDER_NOT_ALLOWED, false, ChannelOutcomeCategory.negativeFeedback()),
                Arguments.of(IoEventClassification.SENT_TO_IO, false, ChannelOutcomeCategory.progress()),
                Arguments.of(IoEventClassification.READ, true, ChannelOutcomeCategory.progress()),
                Arguments.of(IoEventClassification.PAID, true, ChannelOutcomeCategory.progress())
        );
    }

    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @MethodSource("ioClassificationCases")
    void shouldMapCorrectBooleanFlags(IoEventClassification classification, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Assert
        assertEquals(expectedReached, classification.isRecipientReached());
        assertEquals(expectedCategory, classification.getCategory());
    }

    @Test
    void shouldReturnEmptyOptionalWhenDesiredFeedbackIsNull() {
        // Act & Assert
        assertFalse(IoEventClassification.SENDER_NOT_ALLOWED.getSatisfiedDesiredFeedback().isPresent());
        assertFalse(IoEventClassification.SENT_TO_IO.getSatisfiedDesiredFeedback().isPresent());
    }

    @Test
    void shouldReturnCorrectDesiredFeedbackWhenPresent() {
        // Act & Assert
        assertEquals(Optional.of(DesiredFeedbackType.RECEIVED), IoEventClassification.DELIVERED_TO_USER.getSatisfiedDesiredFeedback());
        assertEquals(Optional.of(DesiredFeedbackType.READ), IoEventClassification.READ.getSatisfiedDesiredFeedback());
        assertEquals(Optional.of(DesiredFeedbackType.PAID), IoEventClassification.PAID.getSatisfiedDesiredFeedback());
    }

    @ParameterizedTest(name = "Input: {0}")
    @ValueSource(strings = {"DELIVERED_TO_USER", "delivered_to_user", "Delivered_To_User"})
    void shouldResolveEnumCaseInsensitive(String input) {
        // Act
        IoEventClassification result = IoEventClassification.fromEventType(input);

        // Assert
        assertEquals(IoEventClassification.DELIVERED_TO_USER, result);
    }

    @Test
    void shouldThrowExceptionWhenCodeIsUnknown() {
        // Arrange
        String unknownType = "INVALID_EVENT_TYPE";

        // Act & Assert
        assertThrows(
                PnUnknownEventCodeException.class,
                () -> IoEventClassification.fromEventType(unknownType)
        );
    }
}