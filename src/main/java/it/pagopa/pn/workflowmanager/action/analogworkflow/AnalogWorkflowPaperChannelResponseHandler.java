package it.pagopa.pn.workflowmanager.action.analogworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PrepareAnalogDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.RecipientRelatedTimelineElementDetails;
import it.pagopa.pn.workflowmanager.exceptions.PnPaperChannelChangedCostException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.PrepareEventInt;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED;

@Component
@AllArgsConstructor
@Slf4j
public class AnalogWorkflowPaperChannelResponseHandler {

    private final NotificationService notificationService;
    private final PaperChannelService paperChannelService;
    private final PaperChannelUtils paperChannelUtils;
    private final AuditLogService auditLogService;
    private final TimelineUtils timelineUtils;

    public void paperChannelPrepareResponseHandler(PrepareEventInt response) {

        log.info("paperChannelPrepareResponseHandler response iun={} requestId={} statusCode={} statusDesc={} statusDate={}", response.getIun(), response.getRequestId(), response.getStatusCode(), response.getStatusDetail(), response.getStatusDateTime());

        NotificationInt notification = notificationService.getInformalNotificationByIun(response.getIun());
        TimelineElementInternal timelineElementInternal = paperChannelUtils.getPaperChannelNotificationTimelineElement(response.getIun(), response.getRequestId());

        int recIndex = ((RecipientRelatedTimelineElementDetails)timelineElementInternal.getDetails()).getRecIndex();
        String requestId = response.getRequestId();
        String msg = "Analog workflow Paper channel prepare response requestId={} statusCode={}";
        PnAuditLogEvent auditLogEvent = buildPrepareEventAuditLog(response.getIun(), recIndex, requestId, msg, response.getStatusCode());
        try {
            PrepareEventInt.STATUS_CODE statusCode = PrepareEventInt.STATUS_CODE.valueOf(response.getStatusCode());

            if (statusCode == PrepareEventInt.STATUS_CODE.OK) {
                handlerPrepareOK(response, notification, timelineElementInternal, recIndex, requestId, auditLogEvent);
            } else if (statusCode == PrepareEventInt.STATUS_CODE.KO) {
                handlePrepareKO(response, timelineElementInternal, requestId);
            }
        } catch (Exception exc) {
            auditLogEvent.generateFailure("Unexpected error", exc).log();
            throw exc;
        }
    }

    private void handlerPrepareOK(PrepareEventInt response, NotificationInt notification, TimelineElementInternal timelineElementInternal, int recIndex, String requestId, PnAuditLogEvent auditLogEvent) {
        PhysicalAddressInt receiverAddress = response.getReceiverAddress();
        String productType = response.getProductType();
        List<String> replacedF24AttachmentUrls = response.getReplacedF24AttachmentUrls();
        CategorizedAttachmentsResultInt categorizedAttachmentsResult = response.getCategorizedAttachmentsResult();
        //se era una prepare di un analog, procedo con la send della simpleregistered
        if (isRegisteredLetterDelivery(timelineElementInternal)) {
            log.info("paperChannelPrepareResponseHandler prepare response is for simple registered letter, now registered letter can be sent iun={} requestId={} statusCode={} statusDesc={} statusDate={}", response.getIun(), response.getRequestId(), response.getStatusCode(), response.getStatusDetail(), response.getStatusDateTime());
            try {
                String timelineId =
                        this.paperChannelService.sendSimpleRegisteredLetter(
                                notification,
                                recIndex,
                                requestId,
                                receiverAddress,
                                productType,
                                replacedF24AttachmentUrls,
                                categorizedAttachmentsResult);
                String auditlogmessage = timelineId == null ? "nothing send" : "generated timelineId=" + timelineId;
                auditLogEvent.generateSuccess(auditlogmessage).log();
            } catch (PnPaperChannelChangedCostException e) {
                String auditlogmessage = "send cost is different from prepare cost, need to re-do prepare";
                this.paperChannelService.prepareSimpleRegisteredLetter(notification, recIndex,
                        timelineUtils.retrieveCoverpageFileKey(notification.getIun(),recIndex));
                auditLogEvent.generateWarning(auditlogmessage).log();
            }
        } else {
            auditLogEvent.generateFailure("Unexpected detail of timelineElement on OK event timeline=" + requestId).log();
            throw new PnInternalException("Unexpected detail of timelineElement timeline=" + requestId, ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
        }
    }

    private void handlePrepareKO(PrepareEventInt response, TimelineElementInternal timelineElementInternal, String requestId) {
        if (isRegisteredLetterDelivery(timelineElementInternal)) {
            log.error("paperChannelPrepareResponseHandler prepare response is for simple registered letter event is KO and is " +
                    "not expected iun={} requestId={} statusCode={} statusDesc={} statusDate={}", response.getIun(),
                    response.getRequestId(), response.getStatusCode(), response.getStatusDetail(), response.getStatusDateTime());
            throw new PnInternalException("Unexpected KO for simple registered letter requestId=" + requestId, ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
        } else {
            throw new PnInternalException("Unexpected detail of timelineElement timeline=" + requestId, ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
        }
    }

    private PnAuditLogEvent buildPrepareEventAuditLog(String iun, int recIndex, String requestId, String msg,String statusCode) {
        return auditLogService.buildAuditLogEvent(iun, recIndex, PnAuditLogEventType.AUD_COM_PD_PREPARE_RECEIVE, msg, requestId,statusCode);
    }

    private boolean isRegisteredLetterDelivery(TimelineElementInternal timelineElementInternal) {
        return timelineElementInternal.getDetails() instanceof PrepareAnalogDeliveryDetailsInt sendAnalogDetails
                && Objects.nonNull(sendAnalogDetails.getDeliveryType())
                && sendAnalogDetails.getDeliveryType().equals(AnalogDeliveryTypeInt.RS);
    }
}
