package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.workflowmanager.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnDeliveryClientImplTest {

    @Mock
    private InternalOnlyApi pnDeliveryApi;

    @InjectMocks
    private PnDeliveryClientImpl pnDeliveryClient;

    @Test
    void getSentInformalNotificationReturnsNotificationBody() {
        String iun = "test-iun";
        InformalSentNotificationV1 notification = new InformalSentNotificationV1();
        when(pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo(iun, true))
                .thenReturn(ResponseEntity.ok(notification));

        InformalSentNotificationV1 result = pnDeliveryClient.getSentInformalNotification(iun);

        assertEquals(notification, result);
    }

    @Test
    void getSentInformalNotificationPropagatesExceptionFromApi() {
        String iun = "test-iun";
        when(pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo(iun, true))
                .thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class, () -> pnDeliveryClient.getSentInformalNotification(iun));
    }
}