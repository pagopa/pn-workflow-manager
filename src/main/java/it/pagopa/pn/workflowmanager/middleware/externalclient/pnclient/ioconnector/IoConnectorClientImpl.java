package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.PagoPaInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.api.IoConnectorApi;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.model.Attachment;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.model.MessageRequest;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.ioconnector.model.PaymentData;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@CustomLog
@RequiredArgsConstructor
@Component
public class IoConnectorClientImpl implements IoConnectorClient {
    private final IoConnectorApi ioConnectorApi;
    private final PnWorkflowManagerConfigs cfg;

    @Override
    public void sendMessage(IoMessageRequest ioMessageRequest) {
        log.logInvokingExternalService(CLIENT_NAME, SEND_MESSAGE);

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setRequestId(ioMessageRequest.getRequestId());
        messageRequest.setIun(ioMessageRequest.getNotificationInt().getIun());
        messageRequest.setRecipientTaxId(ioMessageRequest.getNotificationRecipientInt().getTaxId());
        messageRequest.senderServiceId(ioMessageRequest.getCampaign().getServiceId());
        messageRequest.setSubject(ioMessageRequest.getNotificationRecipientInt().getMessage().getPrimaryMessage().getSubject());
        messageRequest.setMarkdown(ioMessageRequest.getMarkdown());
        messageRequest.setAttachments(mapToIoAttachment(ioMessageRequest.getNotificationInt().getDocuments()));
        messageRequest.setSensitiveContent(ioMessageRequest.getCampaign().getSensitiveContent());

        Optional<PagoPaInt> optPaymentInfo = getPaymentInfoFromNotification(ioMessageRequest.getNotificationRecipientInt());
        if(optPaymentInfo.isPresent()) {
            PagoPaInt paymentInfo = optPaymentInfo.get();
            messageRequest.setDueDate(paymentInfo.getDueDate() != null ? paymentInfo.getDueDate().toString() : null);
            messageRequest.setPaymentData(mapToIoPayment(paymentInfo));
        }
        messageRequest.setPollingMaxMins(cfg.getIoPollingMaxMins());
        ioConnectorApi.sendIOMessage(cfg.getCxId(), messageRequest);
    }

    private List<Attachment> mapToIoAttachment(List<NotificationDocumentInt> documents) {
        if(CollectionUtils.isEmpty(documents)) {
            return null;
        }

        return documents.stream()
            .map(
            notDoc -> new Attachment()
                    .fileKey(notDoc.getRef().getKey())
                    .id(notDoc.getRef().getKey())
                    .name(notDoc.getRef().getKey())
            ).toList();
    }

    private Optional<PagoPaInt> getPaymentInfoFromNotification(NotificationRecipientInt notificationRecipientInt) {
        if(CollectionUtils.isEmpty(notificationRecipientInt.getPayments())) {
            return Optional.empty();
        }

        if(notificationRecipientInt.getPayments().size() > 1) {
            log.warn("More than one payment info found for recipient {}. Using the first one.", notificationRecipientInt.getTaxId());
        }

        return Optional.of(notificationRecipientInt.getPayments().getFirst().getPagoPA());
    }

    private PaymentData mapToIoPayment(PagoPaInt paymentInfo) {
        return new PaymentData()
                .amount(paymentInfo.getAmount())
                .creditorTaxId(paymentInfo.getCreditorTaxId())
                .invalidAfterDueDate(paymentInfo.getDueDate() != null)
                .noticeCode(paymentInfo.getNoticeCode());
    }
}
