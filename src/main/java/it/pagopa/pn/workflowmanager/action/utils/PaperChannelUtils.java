package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.action.utils.PnConstants.FIRST_ATTEMPT;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@Service
@Slf4j
@AllArgsConstructor
public class PaperChannelUtils {
    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final TimelineUtils timelineUtils;
    private final TimelineService timelineService;
    private final AttachmentUtils attachmentUtils;
    private final CampaignService campaignService;
    private final WorkflowUtils workflowUtils;

    public PhysicalAddressInt getSenderAddress() {
        return pnWorkflowManagerConfigs.getPaperChannel().getSenderPhysicalAddress();
    }

    public String addSendAnalogNotificationToTimeline(
            NotificationInt notification,
            PhysicalAddressInt physicalAddress,
            Integer recIndex,
            AnalogDtoInt analogDtoInfo,
            List<String> replacedF24AttachmentUrls,
            CategorizedAttachmentsResultInt categorizedAttachmentsResult,
            ServiceLevelInt serviceLevelInt,
            AnalogDeliveryTypeInt analogDeliveryType
    ) {
        TimelineElementInternal timelineElementInternal = timelineUtils.buildSendAnalogNotificationTimelineElement(
                physicalAddress, recIndex, notification, analogDtoInfo, replacedF24AttachmentUrls, categorizedAttachmentsResult,serviceLevelInt, analogDeliveryType);
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
                .relatedRequestId(null) // Per l'invio di una notifica bonaria si presuppone che ci sia un solo invio
                .productType(productType)
                .prepareRequestId(prepareRequestId)
                .build();
    }

    public TimelineElementInternal getPrepareAnalogDeliveryTimelineElement(String iun, String eventId) {
        //Viene ottenuto l'oggetto di timeline
        Optional<TimelineElementInternal> timelineElement = timelineService.getTimelineElement(iun, eventId);

        if (timelineElement.isPresent()) {
            return timelineElement.get();
        } else {
            log.error("There isn't timelineElement - iun {} eventId {}", iun, eventId);
            throw new PnInternalException("There isn't timelineElement - iun " + iun + " eventId " + eventId, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }
    }

    public List<String> retrieveAttachmentsToSend(NotificationInt notification, int recIndex) {
        return attachmentUtils.retrieveAttachments(
                notification, recIndex,
                attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.ANALOG),
                false
        );
    }

    public void scheduleTimeoutForAnalogChannel(NotificationInt notification, int recIndex) {
        Campaign campaign = campaignService.getCampaignByCampaignIdAndSenderId(notification.getCampaignId(), notification.getSender().getPaId());
        workflowUtils.scheduleTimeoutForCurrentChannel(notification.getIun(), recIndex, campaign, ChannelType.ANALOG);
    }

    public String getSendAnalogRequestIdFromPrepareRequestId(String iun, String prepareRequestId) {
        Set<TimelineElementInternal> timeline = timelineService.getTimeline(iun, false);
        return timeline.stream()
                .filter(timelineElement -> timelineElement.getDetails() instanceof SendAnalogMessageDetailsInt)
                .filter(timelineElement -> ((SendAnalogMessageDetailsInt) timelineElement.getDetails()).getPrepareRequestId().equals(prepareRequestId))
                .map(TimelineElementInternal::getElementId)
                .findFirst()
                .orElseThrow(() -> new PnInternalException("SendRequestId is not present for iun=" + iun + " prepareRequestId=" + prepareRequestId, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT));
    }
}
