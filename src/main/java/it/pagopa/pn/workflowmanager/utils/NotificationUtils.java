package it.pagopa.pn.workflowmanager.utils;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationUtils {

    public NotificationRecipientInt getRecipientFromIndex(NotificationInt notification, int index){
        return notification.getRecipients().get(index);
    }

}
