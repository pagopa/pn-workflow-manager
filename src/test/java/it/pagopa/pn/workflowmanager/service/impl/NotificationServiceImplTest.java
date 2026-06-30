package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnHttpResponseException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

class NotificationServiceImplTest {

    @Mock
    private PnDeliveryClient pnDeliveryClient;

    private NotificationServiceImpl service;

    @BeforeEach
    void setup() {
        service = new NotificationServiceImpl(pnDeliveryClient);
    }


    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIun() {
        InformalSentNotificationV1 sentInformalNotification = new InformalSentNotificationV1();
        sentInformalNotification.setIun("001");
        sentInformalNotification.setRecipients(Collections.emptyList());

        Mockito.when(pnDeliveryClient.getSentInformalNotification("001")).thenReturn(sentInformalNotification);

        NotificationInt actual = service.getInformalNotificationByIun("001");

        Assertions.assertEquals("001", actual.getIun());
        Assertions.assertEquals(Collections.emptyList(), actual.getRecipients());
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIunNotFound() {
        Mockito.when(pnDeliveryClient.getSentInformalNotification("001")).thenThrow(PnHttpResponseException.class);

        Assertions.assertThrows(PnHttpResponseException.class, () -> service.getInformalNotificationByIun("001"));
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void getInformalNotificationByIunThrowsPnInternalExceptionWhenNull() {
        Mockito.when(pnDeliveryClient.getSentInformalNotification("003")).thenReturn(null);

        Assertions.assertThrows(PnInternalException.class, () -> service.getInformalNotificationByIun("003"));
    }
}