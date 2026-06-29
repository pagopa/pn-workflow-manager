package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.delivery.model.InformalSentNotificationV1;
import it.pagopa.pn.workflowmanager.dto.notification.common.NotificationInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTIFICATIONFAILED;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final PnDeliveryClient pnDeliveryClient;

    public NotificationServiceImpl(PnDeliveryClient pnDeliveryClient) {
        this.pnDeliveryClient = pnDeliveryClient;
    }


    @Override
    public NotificationInt getInformalNotificationByIun(String iun) {
        InformalSentNotificationV1 sentInformalNotification = pnDeliveryClient.getSentInformalNotification(iun);
        log.debug("Get informal notification OK for - iun {}", iun);

        if (sentInformalNotification != null) {
            return NotificationMapper.externalToInternal(sentInformalNotification);
        } else {
            log.error("Get informal notification is not valid for - iun {}", iun);
            throw new PnInternalException("Get informal notification is not valid for - iun " + iun, ERROR_CODE_DELIVERYPUSH_NOTIFICATIONFAILED);
        }
    }
}
