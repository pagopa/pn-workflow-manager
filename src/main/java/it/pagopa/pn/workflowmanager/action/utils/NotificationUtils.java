package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class NotificationUtils {
    private final AttachmentUtils attachmentUtils;

    public static NotificationRecipientInt getRecipientFromIndex(NotificationInt notification, int index){
        return notification.getRecipients().get(index);
    }

    public List<String> retrieveAttachmentsToSend(NotificationInt notification, int recIndex) {
        return attachmentUtils.retrieveAttachments(
                notification, recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.ANALOG),
                false
        );
    }
}
