package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.timeline.DeliveryModeInt;

import java.util.List;

public interface PnExternalChannelsClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS;

    String LEGAL_NOTIFICATION_REQUEST = "LEGAL NOTIFICATION_REQUEST";

    String COURTESY_NOTIFICATION_REQUEST = "COURTESY NOTIFICATION_REQUEST";
    void sendNotificationPEC(
        String requestId,
        String mailBody,
        NotificationInt notificationInt,
        NotificationRecipientInt recipientInt,
        LegalDigitalAddressInt digitalAddress,
        List<String> fileKeys
    );

    void sendNotificationEMAIL(
            String requestId,
            String mailBody,
            NotificationInt notificationInt,
            NotificationRecipientInt recipientInt,
            DigitalAddressInt digitalAddress,
            String aarKey,
            String quickAccessToken,
            DeliveryModeInt deliveryMode
    );
}
