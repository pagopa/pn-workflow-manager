package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.commons.pnclients.RestTemplateFactory;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.config.msclient.DeliveryApiConfigurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "pn.workflow-manager.delivery-base-url=http://localhost:9999"
        }
)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PnDeliveryClientImpl.class
        , DeliveryApiConfigurator.class,
        PnWorkflowManagerConfigs.class,
        RestTemplateFactory.class})
class PnDeliveryClientImplTest {

    @Mock
    private InternalOnlyApi pnDeliveryApi;

    @Autowired
    private PnDeliveryClientImpl client;

    @BeforeEach
    void setup() {
        client = new PnDeliveryClientImpl(pnDeliveryApi);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getSentInformalNotification() {
        InformalSentNotificationV1 notification = new InformalSentNotificationV1();
        notification.setIun("002");

        Mockito.when(pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo("002")).thenReturn(ResponseEntity.ok(notification));

        InformalSentNotificationV1 res = client.getSentInformalNotification("002");

        Assertions.assertEquals("002", res.getIun());
    }
}