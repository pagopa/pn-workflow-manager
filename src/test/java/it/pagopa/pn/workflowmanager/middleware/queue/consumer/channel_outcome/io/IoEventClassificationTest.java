package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io;

import it.pagopa.pn.workflowmanager.exceptions.PnUnknownEventCodeException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelOutcomeCategory;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IoEventClassificationTest {
    @ParameterizedTest(name = "Tipo {0} -> recipientReached={1}, category={2}")
    @CsvSource({
            "DELIVERED_TO_USER, true,  PROGRESS",
            "SENDER_NOT_ALLOWED, false, FEEDBACK",
            "SENT_TO_IO,         false, PROGRESS",
            "READ,               true,  PROGRESS",
            "PAID,               true,  PROGRESS"
    })
    void shouldMapCorrectBooleanFlags(String enumName, boolean expectedReached, ChannelOutcomeCategory expectedCategory) {
        // Act
        IoEventClassification classification = IoEventClassification.valueOf(enumName);

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