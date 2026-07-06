package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.workflowmanager.action.utils.FileUtils;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalCourtesyMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.api.DigitalLegalMessagesApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.DigitalNotificationRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SENDEMAILNOTIFICATIONFAILED;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SENDPECNOTIFICATIONFAILED;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_SENDSMSNOTIFICATIONFAILED;

@Component
@CustomLog
@RequiredArgsConstructor
public class PnExternalChannelsClientImpl implements PnExternalChannelsClient {
    private static final String EVENT_TYPE_INFORMAL = "INFORMAL";

    private final PnWorkflowManagerConfigs cfg;
    private final DigitalLegalMessagesApi digitalLegalMessagesApi;
    private final DigitalCourtesyMessagesApi digitalCourtesyMessagesApi;

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

    @Override
    public void sendNotificationEMAIL(String requestId,
                                      String mailBody,
                                      NotificationInt notificationInt,
                                      NotificationRecipientInt recipientInt,
                                      DigitalAddressInt digitalAddress,
                                      List<String> attachmentUrls) {
        try {
            log.logInvokingAsyncExternalService(CLIENT_NAME, COURTESY_NOTIFICATION_REQUEST + "[EMAIL]", requestId);
            log.debug("[enter] sendNotificationEMAIL address={} requestId={} recipient={}", LogUtils.maskNumber(digitalAddress.getAddress()), requestId, LogUtils.maskGeneric(recipientInt.getDenomination()));

            DigitalCourtesyMailRequest digitalNotificationRequest = buildDigitalCourtesyMailRequest(requestId, mailBody, recipientInt, digitalAddress, attachmentUrls);

            digitalCourtesyMessagesApi.sendDigitalCourtesyMessage(requestId, cfg.getCxId(), digitalNotificationRequest);

            log.debug("[exit] sendNotificationEMAIL address={} requestId={} recipient={}", LogUtils.maskEmailAddress(digitalAddress.getAddress()), requestId, LogUtils.maskGeneric(recipientInt.getDenomination()));
        } catch (Exception e) {
            log.error("error sending EMAIL notification for iun={}", notificationInt.getIun());
            throw new PnInternalException("error sending EMAIL notification", ERROR_CODE_WORKFLOWMANAGER_SENDEMAILNOTIFICATIONFAILED,e);
        }
    }

    private static @NotNull DigitalCourtesyMailRequest buildDigitalCourtesyMailRequest(String requestId, String mailBody, NotificationRecipientInt recipientInt, DigitalAddressInt digitalAddress, List<String> attachmentUrls) {
        DigitalCourtesyMailRequest digitalNotificationRequest = new DigitalCourtesyMailRequest();
        digitalNotificationRequest.setChannel(DigitalCourtesyMailRequest.ChannelEnum.EMAIL);
        digitalNotificationRequest.setRequestId(requestId);
        digitalNotificationRequest.setCorrelationId(requestId);
        digitalNotificationRequest.setEventType(EVENT_TYPE_INFORMAL);
        digitalNotificationRequest.setQos(DigitalCourtesyMailRequest.QosEnum.BATCH);
        digitalNotificationRequest.setReceiverDigitalAddress(digitalAddress.getAddress());
        digitalNotificationRequest.setClientRequestTimeStamp(Instant.now());
        digitalNotificationRequest.setMessageContentType(DigitalCourtesyMailRequest.MessageContentTypeEnum.TEXT_HTML);
        digitalNotificationRequest.setMessageText(mailBody);
        digitalNotificationRequest.setSubjectText(recipientInt.getMessage().getPrimaryMessage().getSubject());
        digitalNotificationRequest.setAttachmentUrls(attachmentUrls.stream().map(FileUtils::getKeyWithStoragePrefix).toList());
        return digitalNotificationRequest;
    }

    @Override
    public void sendNotificationSMS(
            String requestIdx,
            String textMessage,
            String senderDigitalAddress
    ) {
        try {
            log.logInvokingAsyncExternalService(CLIENT_NAME, COURTESY_NOTIFICATION_REQUEST + "[SMS]", requestIdx);
            log.debug("[enter] sendNotificationSMS requestId={} senderDigitalAddress={}", requestIdx, LogUtils.maskNumber(senderDigitalAddress));

            DigitalCourtesySmsRequest digitalNotificationRequest = new DigitalCourtesySmsRequest();
            digitalNotificationRequest.setChannel(DigitalCourtesySmsRequest.ChannelEnum.SMS);
            digitalNotificationRequest.setRequestId(requestIdx);
            digitalNotificationRequest.setCorrelationId(requestIdx);
            digitalNotificationRequest.setEventType(EVENT_TYPE_INFORMAL);
            digitalNotificationRequest.setQos(DigitalCourtesySmsRequest.QosEnum.BATCH);
            digitalNotificationRequest.setReceiverDigitalAddress(senderDigitalAddress);
            digitalNotificationRequest.setClientRequestTimeStamp(Instant.now());
            digitalNotificationRequest.setMessageText(textMessage);

            digitalCourtesyMessagesApi.sendCourtesyShortMessage(requestIdx, cfg.getCxId(), digitalNotificationRequest);
            log.debug("[exit] sendNotificationSMS requestId={} senderDigitalAddress={}", requestIdx, LogUtils.maskNumber(senderDigitalAddress));
        } catch (Exception e) {
            log.error("error sending SMS notification for requestIdx={}", requestIdx);
            throw new PnInternalException("error sending SMS notification", ERROR_CODE_WORKFLOWMANAGER_SENDSMSNOTIFICATIONFAILED, e);
        }
    }
}
