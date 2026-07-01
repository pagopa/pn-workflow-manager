package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.FileUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalLegalMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalNotificationRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SENDPECNOTIFICATIONFAILED;

@Component
@CustomLog
@RequiredArgsConstructor
public class PnExternalChannelsClientImpl implements PnExternalChannelsClient {
    private static final String EVENT_TYPE_INFORMAL = "INFORMAL";
    private final PnWorkflowManagerConfigs cfg;
    private final DigitalLegalMessagesApi digitalLegalMessagesApi;

    @Override
    public void sendNotificationPEC(
            String requestId,
            String mailBody,
            NotificationInt notificationInt,
            NotificationRecipientInt recipientInt,
            LegalDigitalAddressInt digitalAddress,
            List<String> fileKeys
    ) {
        try {
            log.logInvokingAsyncExternalService(CLIENT_NAME, LEGAL_NOTIFICATION_REQUEST, requestId);

            List<String> fileKeysWithStoragePrefix = fileKeys.stream().map(FileUtils::getKeyWithStoragePrefix).toList();

            DigitalNotificationRequest digitalNotificationRequest = new DigitalNotificationRequest();
            digitalNotificationRequest.setChannel(DigitalNotificationRequest.ChannelEnum.PEC);
            digitalNotificationRequest.setRequestId(requestId);
            digitalNotificationRequest.setCorrelationId(requestId);
            digitalNotificationRequest.setEventType(EVENT_TYPE_INFORMAL);
            digitalNotificationRequest.setMessageContentType(DigitalNotificationRequest.MessageContentTypeEnum.TEXT_HTML);
            digitalNotificationRequest.setQos(DigitalNotificationRequest.QosEnum.BATCH);
            digitalNotificationRequest.setReceiverDigitalAddress(digitalAddress.getAddress());
            digitalNotificationRequest.setClientRequestTimeStamp(Instant.now());
            digitalNotificationRequest.setMessageText(mailBody);
            digitalNotificationRequest.setSubjectText(recipientInt.getMessage().getPrimaryMessage().getSubject());
            digitalNotificationRequest.setAttachmentUrls(fileKeysWithStoragePrefix);


            digitalLegalMessagesApi.sendDigitalLegalMessage(requestId, cfg.getCxId(), digitalNotificationRequest);
        } catch (Exception e) {
            log.error("error sending PEC notification for iun={}", notificationInt.getIun());
            throw new PnInternalException("error sending PEC notification", ERROR_CODE_WORKFLOWMANAGER_SENDPECNOTIFICATIONFAILED, e);
        }
    }
}
