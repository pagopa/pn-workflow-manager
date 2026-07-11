package it.pagopa.pn.workflowmanager.service;

import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;

import java.util.List;

public interface PaperChannelService {
    void prepareSimpleRegisteredLetter(NotificationInt notification, Integer recIndex, String coverpageFileKey);
    String sendSimpleRegisteredLetter(NotificationInt notification,
                                    Integer recIndex,
                                    String prepareRequestId,
                                    PhysicalAddressInt receiverAddress,
                                    String productType,
                                    List<String> replacedF24AttachmentUrls,
                                    CategorizedAttachmentsResultInt categorizedAttachmentsResult);
}
