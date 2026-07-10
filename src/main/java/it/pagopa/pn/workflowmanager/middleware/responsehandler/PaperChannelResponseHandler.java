package it.pagopa.pn.workflowmanager.middleware.responsehandler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.*;
import it.pagopa.pn.workflowmanager.action.analogworkflow.AnalogWorkflowPaperChannelResponseHandler;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.AttachmentDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;

import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterEnum;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel.PaperMessagesClient;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.analog.AnalogEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.PrepareEventInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.SendEventInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.HandleEventUtils;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED;

@Component
@CustomLog
@AllArgsConstructor
public class PaperChannelResponseHandler {
    public static final String EXCEPTION_PREPARE_UPDATE = "Exception PrepareUpdate";
    private final AnalogWorkflowPaperChannelResponseHandler analogWorkflowPaperChannelResponseHandler;
    private final TimelineUtils timelineUtils;
    private final ChannelEventProcessor channelEventProcessor;
    private final AnalogEventNormalizer analogEventNormalizer;

    /**
     * Handle notification response from external channel. Positive response means notification is delivered correctly, so the workflow can be completed successfully.
     * Negative response means notification could not be delivered to the indicated address.
     *
     * @param response Notification response
     */
    public void paperChannelResponseReceiver(PaperChannelUpdate response) {
        if (response.getPrepareEvent() != null)
            prepareUpdate(response.getPrepareEvent());
        else if (response.getSendEvent() != null)
            channelEventProcessor.process(mapExternalToInternal(response.getSendEvent()), analogEventNormalizer);
        else
            handleError(response);
    }

    private void prepareUpdate(PrepareEvent event) {
        String iun = timelineUtils.getIunFromTimelineId(event.getRequestId());
        addMdcFilter(iun, event.getRequestId());
        log.info("Async response received from service {} for {} with correlationId={}",
                PaperMessagesClient.CLIENT_NAME, PaperMessagesClient.PREPARE_ANALOG_NOTIFICATION, event.getRequestId());

        final String processName = PaperMessagesClient.PREPARE_ANALOG_NOTIFICATION + " response handler";

        try {
            log.logStartingProcess(processName);

            PrepareEventInt analogSentResponseInt = mapExternalToInternal(iun, event);

            log.debug("Received PaperChannel prepare paper message event for requestId={} - status={} details={} receiverAddress={}",
                    analogSentResponseInt.getRequestId(), analogSentResponseInt.getStatusCode(), analogSentResponseInt.getStatusDetail(), (analogSentResponseInt.getReceiverAddress()==null?"": LogUtils.maskGeneric(analogSentResponseInt.getReceiverAddress().getAddress())));

            analogWorkflowPaperChannelResponseHandler.paperChannelPrepareResponseHandler(analogSentResponseInt);

            log.logEndingProcess(processName);

        } catch (PnRuntimeException e) {
            log.logEndingProcess(processName, false, e.getMessage(), e);
            log.error(EXCEPTION_PREPARE_UPDATE, e);
            throw e;
        } catch (Exception e) {
            log.logEndingProcess(processName, false, e.getMessage(), e);
            log.error(EXCEPTION_PREPARE_UPDATE, e);
            throw new PnInternalException("Paper update failed", ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED, e);
        }
    }

    private PrepareEventInt mapExternalToInternal(String iun, PrepareEvent event) {

        // valido l'evento
        validateEvent(event);

        var builder = PrepareEventInt.builder()
                .iun(iun)
                .statusCode(Optional.ofNullable(event.getStatusCode()).map(StatusCodeEnum::getValue).orElse(null))
                .statusDetail(event.getStatusDetail())
                .replacedF24AttachmentUrls(event.getReplacedF24AttachmentUrls())
                .requestId(event.getRequestId())
                .statusDateTime(event.getStatusDateTime())
                .failureDetailCode(Optional.ofNullable(event.getFailureDetailCode()).map(FailureDetailCodeEnum::getValue).orElse(null))
                .productType(event.getProductType());

        if (event.getCategorizedAttachments() != null) {
            CategorizedAttachmentsResult rawCategorizedAttachments = event.getCategorizedAttachments();

            List<ResultFilterInt> acceptedAttachments = rawCategorizedAttachments.getAcceptedAttachments() == null ? null :
                    rawCategorizedAttachments.getAcceptedAttachments().stream()
                    .map(this::mapResultFilterToInternal)
                    .toList();

            List<ResultFilterInt> discardedAttachments = rawCategorizedAttachments.getDiscardedAttachments() == null ? null :
                    rawCategorizedAttachments.getDiscardedAttachments().stream()
                    .map(this::mapResultFilterToInternal)
                    .toList();

            builder.categorizedAttachmentsResult(
                    CategorizedAttachmentsResultInt.builder()
                            .acceptedAttachments(acceptedAttachments)
                            .discardedAttachments(discardedAttachments)
                            .build()
            );
        }

        if (event.getReceiverAddress() != null) {
            AnalogAddress rawAddress = event.getReceiverAddress();

            builder.receiverAddress(
                    PhysicalAddressInt.builder()
                            .fullname(rawAddress.getFullname())
                            .address(rawAddress.getAddress())
                            .addressDetails(rawAddress.getAddressRow2())
                            .municipality(rawAddress.getCity())
                            .municipalityDetails(rawAddress.getCity2())
                            .province(rawAddress.getPr())
                            .zip(rawAddress.getCap())
                            .foreignState(rawAddress.getCountry())
                            .at(rawAddress.getNameRow2())
                            .build()
            );
        }


        return builder.build();
    }

    private ResultFilterInt mapResultFilterToInternal(ResultFilter resultFilter){
        return ResultFilterInt.builder()
                .fileKey(resultFilter.getFileKey())
                .result(ResultFilterEnum.fromValue(
                        Objects.requireNonNull(resultFilter.getResult()).getValue()))
                .reasonCode(resultFilter.getReasonCode())
                .reasonDescription(resultFilter.getReasonDescription())
                .build();
    }

    private void validateEvent(PrepareEvent event){
        // mi aspetto ci sia lo statusCode
        if (event.getStatusCode() == null)
        {
            log.error("No statusCode specified in paperchannelevent event={}", event);
            throw new PnInternalException("No statusCode specified, invalid event update received from paper-channel", ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
        }
        if (event.getStatusCode() == StatusCodeEnum.KO)
        {
            // mi aspetto ci sia il failureDetailCode
            if (event.getFailureDetailCode() == null) {
                log.error("No failureDetailCode specified in paperchannelevent event={}", event);
                throw new PnInternalException("No failureDetailCode specified, invalid event update received from paper-channel", ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
            }
            // nel caso di D01, D02, mi aspetto ci sia anche l'indirizzo
            if ((event.getFailureDetailCode() == FailureDetailCodeEnum.D01 || event.getFailureDetailCode() == FailureDetailCodeEnum.D02)
                    && event.getReceiverAddress() == null)
            {
                log.error("No address specified in paperchannelevent event={}", event);
                throw new PnInternalException("No address specified, invalid event update received from paper-channel", ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
            }
        }
    }

    private SendEventInt mapExternalToInternal(SendEvent event) {
        return SendEventInt.builder()
                .iun(timelineUtils.getIunFromTimelineId(event.getRequestId()))
                .requestId(event.getRequestId())
                .statusCode(Optional.ofNullable(event.getStatusCode()).map(StatusCodeEnum::getValue).orElse(null))
                .statusDateTime(event.getStatusDateTime())
                .statusDetail(event.getStatusDetail())
                .statusDescription(event.getStatusDescription())
                .attachments(event.getAttachments() == null ? null : event.getAttachments().stream()
                                                                     .map(this::mapAttachmentDetailsToInternal)
                                                                     .toList())
                .discoveredAddress(mapAnalogAddressToInternal(event.getDiscoveredAddress()))
                .deliveryFailureCause(Optional.ofNullable(event.getDeliveryFailureCause()).map(Object::toString).orElse(null))
                .registeredLetterCode(event.getRegisteredLetterCode())
                .build();
    }

    private AttachmentDetailsInt mapAttachmentDetailsToInternal(AttachmentDetails attachmentDetails) {
        return AttachmentDetailsInt.builder()
                .id(attachmentDetails.getId())
                .documentType(attachmentDetails.getDocumentType())
                .url(attachmentDetails.getUrl())
                .date(attachmentDetails.getDate())
                .build();
    }

    private PhysicalAddressInt mapAnalogAddressToInternal(AnalogAddress rawAddress) {
        if (rawAddress == null) {
            return null;
        }

        return PhysicalAddressInt.builder()
                .fullname(rawAddress.getFullname())
                .address(rawAddress.getAddress())
                .addressDetails(rawAddress.getAddressRow2())
                .municipality(rawAddress.getCity())
                .municipalityDetails(rawAddress.getCity2())
                .province(rawAddress.getPr())
                .zip(rawAddress.getCap())
                .foreignState(rawAddress.getCountry())
                .at(rawAddress.getNameRow2())
                .build();
    }

    private void handleError(PaperChannelUpdate response) {
        log.error("None event specified in paperchannelevent event={}", response);
        throw new PnInternalException("None event specified, invalid event update received from paper-channel", ERROR_CODE_WORKFLOWMANAGER_PAPERUPDATEFAILED);
    }

    private static void addMdcFilter(String iun, String correlationId) {
        HandleEventUtils.addIunToMdc(iun);
        HandleEventUtils.addCorrelationIdToMdc(correlationId);
    }
}
