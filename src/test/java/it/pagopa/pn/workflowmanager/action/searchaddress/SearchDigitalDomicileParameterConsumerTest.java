package it.pagopa.pn.workflowmanager.action.searchaddress;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SearchDigitalDomicileParameterConsumerTest {

    private ParameterConsumer parameterConsumer;
    private SearchDigitalDomicileParameterConsumer consumer;

    @BeforeEach
    void setup() {
        parameterConsumer = Mockito.mock(ParameterConsumer.class);
        consumer = new SearchDigitalDomicileParameterConsumer(parameterConsumer, new ArrayList<>());
    }

    @Test
    void initializeUnexpectedInternalExceptionIsPropagated() {
        PnInternalException exception = new PnInternalException("boom", "GENERIC_ERROR");

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(SearchDigitalDomicileConfig[].class)))
                .thenThrow(exception);

        Assertions.assertThrows(PnInternalException.class, consumer::initialize);
    }

    @Test
    void initializeLeavesEmptyListWhenParameterDoesNotExist() {
        ParameterNotFoundException notFoundException = ParameterNotFoundException.builder().message("not found").build();
        PnInternalException wrapped = new PnInternalException("missing", "GENERIC_ERROR", notFoundException);

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(SearchDigitalDomicileConfig[].class)))
                .thenThrow(wrapped);

        consumer.initialize();

        Assertions.assertEquals(List.of(), consumer.getSearchDigitalDomicileConfigs());
    }

    @Test
    void initializeLoadsOnlyValidConfigurations() {
        SearchDigitalDomicileConfig valid = new SearchDigitalDomicileConfig(
                Instant.parse("2026-10-01T00:00:00Z"),
                List.of(),
                List.of(DigitalAddressSourceInt.PLATFORM),
                List.of(DigitalAddressSourceInt.SPECIAL)
        );
        SearchDigitalDomicileConfig invalid = new SearchDigitalDomicileConfig(
                null,
                List.of(),
                List.of(),
                List.of()
        );

        Mockito.when(parameterConsumer.getParameterValue(Mockito.anyString(), Mockito.eq(SearchDigitalDomicileConfig[].class)))
                .thenReturn(Optional.of(new SearchDigitalDomicileConfig[]{valid, invalid}));

        consumer.initialize();

        Assertions.assertEquals(1, consumer.getSearchDigitalDomicileConfigs().size());
        Assertions.assertEquals(valid, consumer.getSearchDigitalDomicileConfigs().getFirst());
    }
}
