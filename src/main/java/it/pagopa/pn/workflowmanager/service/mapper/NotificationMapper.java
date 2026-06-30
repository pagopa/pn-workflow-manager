package it.pagopa.pn.workflowmanager.service.mapper;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.*;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationMapper {
    private NotificationMapper(){}

    public static NotificationInt externalToInternal(InformalSentNotificationV1 sentInformalNotification) {
        List<NotificationRecipientInt> listNotificationRecipientInt = mapNotificationRecipientInformal(sentInformalNotification.getRecipients());
        List<NotificationDocumentInt> listNotificationDocumentIntInt = mapNotificationDocument(sentInformalNotification.getDocuments());

        return NotificationInt.builder()
                .iun(sentInformalNotification.getIun())
                .subject(sentInformalNotification.getSubject())
                .paProtocolNumber(sentInformalNotification.getPaProtocolNumber())
                .sentAt(sentInformalNotification.getSentAt())
                .sender(
                        NotificationSenderInt.builder()
                                .paTaxId(sentInformalNotification.getSenderTaxId())
                                .paId(sentInformalNotification.getSenderPaId())
                                .paDenomination(sentInformalNotification.getSenderDenomination())
                                .build()
                )
                .documents(listNotificationDocumentIntInt)
                .recipients(listNotificationRecipientInt)
                .group(sentInformalNotification.getGroup())
                .version(sentInformalNotification.getVersion())
                .usedServices(UsedServicesMapper.externalToInternal(sentInformalNotification.getUsedServices()))
                .idempotenceToken(sentInformalNotification.getIdempotenceToken())
                .campaignId(sentInformalNotification.getCampaignId())
                .communicationType(CommunicationType.INFORMAL)
                .build();
    }

    private static List<NotificationDocumentInt> mapNotificationDocument(List<NotificationDocument> documents) {
        List<NotificationDocumentInt> list = new ArrayList<>();

        if (documents == null) {
            return list;
        }

        for (NotificationDocument document : documents){
            NotificationDocumentInt notificationDocumentInt = NotificationDocumentInt.builder()
                    .digests(
                            NotificationDocumentInt.Digests.builder()
                                    .sha256(document.getDigests().getSha256())
                                    .build()
                    )
                    .ref(
                            NotificationDocumentInt.Ref.builder()
                                    .key(document.getRef().getKey())
                                    .versionToken(document.getRef().getVersionToken())
                                    .build()
                    )
                    .build();

            list.add(notificationDocumentInt);
        }

        return list;
    }

    private static List<NotificationRecipientInt> mapNotificationRecipientInformal(List<FullInformalNotificationRecipientV1> recipients) {
        List<NotificationRecipientInt> list = new ArrayList<>();

        if (recipients == null) {
            return list;
        }
        for (FullInformalNotificationRecipientV1 recipient : recipients) {
            NotificationRecipientInt.NotificationRecipientIntBuilder recipientIntBuilder = NotificationRecipientInt.builder()
                    .taxId(recipient.getTaxId())
                    .internalId(recipient.getInternalId())
                    .denomination(recipient.getDenomination())
                    .recipientType(RecipientTypeInt.valueOf(recipient.getRecipientType().name()))
                    .messageId(recipient.getMessageId().toString())
                    .message(mapNotificationMessageInt(recipient.getMessage()))
                    .email(recipient.getEmail())
                    .additionalLanguages(recipient.getAdditionalLanguages())
                    .phoneNumber(recipient.getPhoneNumber());

            NotificationDigitalAddress digitalDomicile = recipient.getDigitalDomicile();
            if (digitalDomicile != null) {
                recipientIntBuilder.digitalDomicile(
                        LegalDigitalAddressInt.builder()
                                .address(digitalDomicile.getAddress())
                                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.valueOf(digitalDomicile.getType().name()))
                                .build());
            }

            NotificationPhysicalAddress physicalAddress = recipient.getPhysicalAddress();
            if (physicalAddress != null) {
                recipientIntBuilder.physicalAddress(
                        PhysicalAddressInt.builder()
                                .fullname(recipient.getDenomination())
                                .at(physicalAddress.getAt())
                                .address(physicalAddress.getAddress())
                                .addressDetails(physicalAddress.getAddressDetails())
                                .foreignState(physicalAddress.getForeignState())
                                .municipality(physicalAddress.getMunicipality())
                                .municipalityDetails(physicalAddress.getMunicipalityDetails())
                                .province(physicalAddress.getProvince())
                                .zip(physicalAddress.getZip())
                                .build());
            }

            recipientIntBuilder.payments(mapNotificationPaymentInfo(recipient.getPayments()));
            list.add(recipientIntBuilder.build());
        }

        return list;
    }

    private static List<NotificationPaymentInfoInt> mapNotificationPaymentInfo(List<InformalNotificationPaymentItem> payments) {
        List<NotificationPaymentInfoInt> list = new ArrayList<>();

        if (payments == null) {
            return list;
        }

        for (InformalNotificationPaymentItem payment : payments) {
            PagoPaPaymentBase pagoPa = payment.getPagoPa();
            list.add(
                    NotificationPaymentInfoInt.builder()
                            .pagoPA(PagoPaInt.builder()
                                    .creditorTaxId(pagoPa.getCreditorTaxId())
                                    .noticeCode(pagoPa.getNoticeCode())
                                    .amount(pagoPa.getAmount())
                                    .dueDate(pagoPa.getDueDate())
                                    .attachment(pagoPa.getAttachment() != null ? NotificationDocumentInt.builder()
                                            .ref(NotificationDocumentInt.Ref.builder()
                                                    .key(pagoPa.getAttachment().getRef().getKey())
                                                    .versionToken(pagoPa.getAttachment().getRef().getVersionToken())
                                                    .build())
                                            .digests(NotificationDocumentInt.Digests.builder()
                                                    .sha256(pagoPa.getAttachment().getDigests().getSha256())
                                                    .build())
                                            .build() : null)
                                    .build())
                            .build());
        }

        return list;
    }

    private static NotificationMessageInt mapNotificationMessageInt(NewMessageRequest message) {
        if (message == null) {
            return null;
        }

        NotificationMessageInt.NotificationMessageIntBuilder builder = NotificationMessageInt.builder()
                .primaryMessage(
                        LocalizedMessageInt.builder()
                                .language(message.getPrimaryMessage().getLanguage())
                                .subject(message.getPrimaryMessage().getSubject())
                                .longBody(message.getPrimaryMessage().getLongBody())
                                .shortBody(message.getPrimaryMessage().getShortBody())
                                .build()
                );

        if(message.getAdditionalMessage() != null) {
            builder.additionalMessage(
                    LocalizedMessageInt.builder()
                            .language(message.getAdditionalMessage().getLanguage())
                            .subject(message.getAdditionalMessage().getSubject())
                            .longBody(message.getAdditionalMessage().getLongBody())
                            .shortBody(message.getAdditionalMessage().getShortBody())
                            .build()
            );
        }

        return builder.build();
    }

}
