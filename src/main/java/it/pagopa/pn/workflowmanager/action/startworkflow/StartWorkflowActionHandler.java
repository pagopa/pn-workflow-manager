package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchOrchestrator;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StartWorkflowActionHandler {
    private final NotificationService notificationService;
    private final AddressSearchOrchestrator addressSearchOrchestrator;

    public void startWorkflowAction(String iun, int recIndex, StartWorkflowDetails startWorkflowDetails) {
        log.info("Start informal notification workflow for recipient - iun {} id {} channel {}",
                iun, recIndex, startWorkflowDetails.getChannel());

        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);

        startAddressSearch(notificationInt, startWorkflowDetails.getChannel(), recIndex);

        log.info("Workflow started successfully for iun {} recipient {}", iun, recIndex);
    }

    private void startAddressSearch(NotificationInt notificationInt, ChannelType channelType, int recIndex) {
        log.debug("Starting address search for iun {} recipient {} channel {}", notificationInt.getIun(), recIndex, channelType);
        AddressSearchContext context = new AddressSearchContext(channelType, notificationInt.getSentAt(), notificationInt, recIndex, 0);
        addressSearchOrchestrator.start(context);
    }
}
