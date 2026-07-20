package it.pagopa.pn.workflowmanager.action.documentcreation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.action.details.DocumentCreationResponseActionDetails;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_UNSUPPORTED_DOCUMENT_CREATION_TYPE;

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
            case COVERPAGE -> paperChannelService.prepareSimpleRegisteredLetter(
                    notificationInt,
                    recIndex
            );
            default -> throw new PnInternalException(
                    String.format("Unsupported document creation type: %s", documentCreationType),
                    ERROR_CODE_WORKFLOWMANAGER_UNSUPPORTED_DOCUMENT_CREATION_TYPE
            );
        }
    }
}
