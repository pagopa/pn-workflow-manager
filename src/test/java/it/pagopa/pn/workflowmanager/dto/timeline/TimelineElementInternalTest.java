package it.pagopa.pn.workflowmanager.dto.timeline;

import it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

class TimelineElementInternalTest {


    @Test
    void compareToBase() {

        Instant tPrimo = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        Instant tSecondo = Instant.EPOCH.plus(2, ChronoUnit.DAYS);

        // caso 1: un xxx con data maggiore va dopo
        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .elementId("elementId1")
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(tSecondo)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();


        Assertions.assertTrue(t1Progress.compareTo(t2Progress) < 0);

        // caso 2: un feedback con stessa data, va dopo
        t2Progress= TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .elementId("elementId2")
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();

        Assertions.assertTrue(t1Progress.compareTo(t2Progress) < 0);

        // caso 3: un progress con data maggiore, va dopo il feedback

        t1Progress = TimelineElementInternal.builder()
                .timestamp(tSecondo)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();
        t2Progress= TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK)
                .build();

        Assertions.assertTrue(t1Progress.compareTo(t2Progress) > 0);
    }

    @Test
    void compareToBaseAnalog() {

        Instant tPrimo = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        Instant tSecondo = Instant.EPOCH.plus(2, ChronoUnit.DAYS);

        // caso 1: un xxx con data maggiore va dopo
        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .elementId("elementId1")
                .category(TimelineElementCategoryInt.SEND_ANALOG_MESSAGE)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(tSecondo)
                .category(TimelineElementCategoryInt.SEND_ANALOG_MESSAGE)
                .build();


        Assertions.assertTrue(t1Progress.compareTo(t2Progress) < 0);

        // caso 2: un feedback con stessa data, va dopo
        t2Progress= TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .elementId("elementId2")
                .category(TimelineElementCategoryInt.SEND_ANALOG_MESSAGE_PROGRESS)
                .build();

        Assertions.assertTrue(t1Progress.compareTo(t2Progress) < 0);

        // caso 3: un progress con data maggiore, va dopo il feedback

        t1Progress = TimelineElementInternal.builder()
                .timestamp(tSecondo)
                .category(TimelineElementCategoryInt.SEND_ANALOG_MESSAGE_PROGRESS)
                .build();
        t2Progress= TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .category(TimelineElementCategoryInt.SEND_ANALOG_MESSAGE_PROGRESS)
                .build();

        Assertions.assertTrue(t1Progress.compareTo(t2Progress) > 0);
    }


    @Test
    void compareTo() {

        Instant t1 = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        Instant t2 = Instant.EPOCH.plus(2, ChronoUnit.DAYS);

        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(t1)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(t2)
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();

        Set<TimelineElementInternal> set = Set.of(t1Progress, t2Progress);
        List<TimelineElementInternal> list = set.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        Assertions.assertEquals(t1Progress, list.get(0));
        Assertions.assertEquals(t2Progress, list.get(1));

    }

    @Test
    void compareToSame() {

        Instant t1 = Instant.EPOCH.plus(1, ChronoUnit.DAYS);

        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(t1)
                .elementId("elementId1")
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(t1)
                .elementId("elementId2")
                .category(TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK)
                .build();

        Set<TimelineElementInternal> set = Set.of(t1Progress, t2Progress);
        List<TimelineElementInternal> list = set.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        Assertions.assertEquals(t1Progress, list.get(0));
        Assertions.assertEquals(t2Progress, list.get(1));

    }
}