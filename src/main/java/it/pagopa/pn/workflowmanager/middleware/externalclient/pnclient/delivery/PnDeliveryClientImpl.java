package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.api.InternalOnlyApi;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@CustomLog
@RequiredArgsConstructor
@Component
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private final InternalOnlyApi pnDeliveryApi;

    @Override
    public InformalSentNotificationV1 getSentInformalNotification(String iun) {
        log.logInvokingExternalService(CLIENT_NAME, GET_INFORMAL_NOTIFICATION);

        ResponseEntity<InformalSentNotificationV1> res = pnDeliveryApi.getSentInformalNotificationPrivateV1WithHttpInfo(iun);

        return res.getBody();
    }
}
