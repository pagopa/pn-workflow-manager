package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;

public interface PnDeliveryClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_DELIVERY;

    String GET_INFORMAL_NOTIFICATION = "GET INFORMAL NOTIFICATION";

    InformalSentNotificationV1 getSentInformalNotification(String iun);
}
