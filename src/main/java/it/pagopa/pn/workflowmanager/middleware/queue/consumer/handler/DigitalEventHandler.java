package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.CourtesyMessageProgressEvent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.LegalMessageSentDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.EmailEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.ExtChannelOutcomeEventCodeInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.PecEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.feedback.extchannel.SmsEventNormalizer;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_DIGITAL_EVENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class DigitalEventHandler {
    private final ChannelEventProcessor channelEventProcessor;
    private final PecEventNormalizer pecEventNormalizer;
    private final EmailEventNormalizer emailEventNormalizer;
    private final SmsEventNormalizer smsEventNormalizer;

    public void handle(SingleStatusUpdate event) {
        if (event.getDigitalLegal() != null) {
            ExtChannelOutcomeEvent legalEvent = mapLegalEvent(event);
            channelEventProcessor.process(legalEvent, pecEventNormalizer);
        } else if (event.getDigitalCourtesy() != null) {
            ExtChannelOutcomeEvent courtesyEvent = mapCourtesyEvent(event);
            ChannelType channelType = courtesyEvent.getEventCode().getChannelType();
            if (channelType == ChannelType.EMAIL) {
                channelEventProcessor.process(courtesyEvent, emailEventNormalizer);
            } else if (channelType == ChannelType.SMS) {
                channelEventProcessor.process(courtesyEvent, smsEventNormalizer);
            } else {
                throw new PnInternalException(
                        "Unsupported courtesy channel type: " + channelType,
                        ERROR_CODE_WORKFLOWMANAGER_INVALID_DIGITAL_EVENT
                );
            }
        } else {
            throw new PnInternalException(
                    "Invalid digital event: both digitalLegal and digitalCourtesy are null",
                    ERROR_CODE_WORKFLOWMANAGER_INVALID_DIGITAL_EVENT
            );
        }
    }

    private ExtChannelOutcomeEvent mapLegalEvent(SingleStatusUpdate event) {
        LegalMessageSentDetails digitalLegal = event.getDigitalLegal();
        return ExtChannelOutcomeEvent.builder()
                .requestId(digitalLegal.getRequestId())
                .eventTimestamp(resolveEventTimestamp(digitalLegal.getEventTimestamp(), event.getEventTimestamp()))
                .status(digitalLegal.getStatus().getValue())
                .eventDetails(digitalLegal.getEventDetails())
                .generatedMessage(digitalLegal.getGeneratedMessage() != null ? digitalLegal.getGeneratedMessage().toString() : null)
                .eventCode(ExtChannelOutcomeEventCodeInt.fromValue(digitalLegal.getEventCode().getValue()))
                .build();
    }

    private ExtChannelOutcomeEvent mapCourtesyEvent(SingleStatusUpdate event) {
        CourtesyMessageProgressEvent digitalCourtesy = event.getDigitalCourtesy();
        return ExtChannelOutcomeEvent.builder()
                .requestId(digitalCourtesy.getRequestId())
                .eventTimestamp(resolveEventTimestamp(digitalCourtesy.getEventTimestamp(), event.getEventTimestamp()))
                .status(digitalCourtesy.getStatus().getValue())
                .eventDetails(digitalCourtesy.getEventDetails())
                .generatedMessage(digitalCourtesy.getGeneratedMessage() != null ? digitalCourtesy.getGeneratedMessage().toString() : null)
                .eventCode(ExtChannelOutcomeEventCodeInt.fromValue(digitalCourtesy.getEventCode().getValue()))
                .build();
    }

    private Instant resolveEventTimestamp(Instant channelTimestamp, Instant envelopeTimestamp) {
        return channelTimestamp != null ? channelTimestamp : envelopeTimestamp;
    }
}
