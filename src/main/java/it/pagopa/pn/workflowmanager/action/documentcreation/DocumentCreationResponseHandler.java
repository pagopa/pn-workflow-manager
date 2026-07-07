package it.pagopa.pn.workflowmanager.action.documentcreation;

import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DocumentCreationResponseHandler{
    private final NotificationService notificationService;
    private final PaperChannelService paperChannelService;

    public void handleResponseReceived(String iun, Integer recIndex, DocumentCreationResponseActionDetails actionDetails) {
        log.info("Start handleDocumentCreationResponse process - iun={} documentCreationResponseId={}", iun, actionDetails.getKey());
        NotificationInt notificationInt = notificationService.getInformalNotificationByIun(iun);
        DocumentCreationType documentCreationType = DocumentCreationType.valueOf(actionDetails.getDocumentCreationType());
        switch(documentCreationType) {
            case DocumentCreationType.COVERPAGE -> paperChannelService.prepareSimpleRegisteredLetter(
                    notificationInt,
                    recIndex,
                    actionDetails.getKey()//ToDo la fileKey è questo dato?
            );
            default -> throw new IllegalArgumentException("Unsupported document creation type: " + documentCreationType);
        }
    }
}
