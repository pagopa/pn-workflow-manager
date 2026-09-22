package it.pagopa.pn.workflowmanager.action.sendcourtesy;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CourtesyRetryableErrorClassifierTest {

    @InjectMocks
    private CourtesyRetryableErrorClassifier classifier;

    private CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel;

    @BeforeEach
    void setUp() {
        channel = CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.EMAIL;
    }

    @Test
    void isRetryableTransportErrorReturnsTrueForNetworkErrors() {
        assertTrue(classifier.isRetryableTransportError(channel, new ResourceAccessException("error", new SocketTimeoutException("timeout"))));
        assertTrue(classifier.isRetryableTransportError(channel, new UnknownHostException("unknown")));
    }

    @Test
    void isRetryableTransportErrorReturnsTrueForRetryableHttpStatuses() {
        assertTrue(classifier.isRetryableTransportError(channel, new WebClientResponseException(
                429, "too many", null, null, null)));
        assertTrue(classifier.isRetryableTransportError(channel, new WebClientResponseException(
                503, "server", null, null, null)));
        assertTrue(classifier.isRetryableTransportError(channel, new PnHttpResponseException("server", 503)));
    }

    @Test
    void isRetryableTransportErrorReturnsFalseForPermanentHttpStatuses() {
        assertFalse(classifier.isRetryableTransportError(channel, new WebClientResponseException(
                400, "bad", null, null, null)));
        assertFalse(classifier.isRetryableTransportError(channel, new PnHttpResponseException("bad", 400)));
    }
}
