package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@Service
@Slf4j
@AllArgsConstructor
public class PaperChannelUtils {
    private static final Integer FIRST_ATTEMPT = 0;

    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;

    public PhysicalAddressInt getSenderAddress() {
        return pnWorkflowManagerConfigs.getPaperChannel().getSenderPhysicalAddress();
    }

    public String addSendAnalogNotificationToTimeline(NotificationInt notification, PhysicalAddressInt physicalAddress, Integer recIndex,
                                                      AnalogDtoInt analogDtoInfo, List<String> replacedF24AttachmentUrls,
                                                      CategorizedAttachmentsResultInt categorizedAttachmentsResult,
                                                      String prepareRequestId,
                                                      ServiceLevelInt serviceLevelInt) {
        TimelineElementInternal timelineElementInternal = timelineUtils.buildSendAnalogNotificationTimelineElement(
                physicalAddress, recIndex, notification, analogDtoInfo, replacedF24AttachmentUrls, categorizedAttachmentsResult,serviceLevelInt,prepareRequestId);
        addTimelineElement(timelineElementInternal,
                notification
        );
        return timelineElementInternal.getElementId();
    }

    private void addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        timelineService.addTimelineElement(element, notification);
    }

    public static List<String> getAttachments(CategorizedAttachmentsResultInt categorizedAttachmentsResult) {
        return categorizedAttachmentsResult.getAcceptedAttachments().stream()
                .map(ResultFilterInt::getFileKey)
                .toList();
    }

    public static AnalogDtoInt buildAnalogDto(String prepareRequestId, String productType, SendResponse sendResponse) {
        return AnalogDtoInt.builder()
                .sentAttemptMade(FIRST_ATTEMPT)
                .sendResponse(sendResponse)
                .relatedRequestId(null)
                .productType(productType)
                .prepareRequestId(prepareRequestId)
                .build();
    }

    public TimelineElementInternal getPaperChannelNotificationTimelineElement(String iun, String eventId) {
        //Viene ottenuto l'oggetto di timeline
        Optional<TimelineElementInternal> timelineElement = timelineService.getTimelineElement(iun, eventId);

        if (timelineElement.isPresent()) {
            return timelineElement.get();
        } else {
            log.error("There isn't timelineElement - iun {} eventId {}", iun, eventId);
            throw new PnInternalException("There isn't timelineElement - iun " + iun + " eventId " + eventId, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }
    }


}
