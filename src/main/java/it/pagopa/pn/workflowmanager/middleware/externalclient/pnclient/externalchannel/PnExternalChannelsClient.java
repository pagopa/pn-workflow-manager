package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;

import java.util.List;

public interface PnExternalChannelsClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_EXTERNAL_CHANNELS;

    String LEGAL_NOTIFICATION_REQUEST = "LEGAL NOTIFICATION_REQUEST";

    void sendNotificationPEC(
        String requestId,
        String mailBody,
        NotificationInt notificationInt,
        NotificationRecipientInt recipientInt,
        LegalDigitalAddressInt digitalAddress,
        List<String> fileKeys
    );
}
