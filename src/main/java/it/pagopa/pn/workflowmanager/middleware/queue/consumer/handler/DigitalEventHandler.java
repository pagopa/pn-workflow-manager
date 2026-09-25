package it.pagopa.pn.workflowmanager.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.CourtesyMessageProgressEvent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.LegalMessageSentDetails;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.externalchannels.model.SingleStatusUpdate;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.ChannelEventProcessor;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.email.EmailEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.pec.PecEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.sms.SmsEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.DigitalMessageReferenceInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEvent;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeEventCodeInt;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.ExtChannelOutcomeStatusInt;
import it.pagopa.pn.workflowmanager.utils.CourtesyEventOriginResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED;

@Component
@Slf4j
@RequiredArgsConstructor
public class DigitalEventHandler {
    private final ChannelEventProcessor channelEventProcessor;
    private final PecEventNormalizer pecEventNormalizer;
    private final EmailEventNormalizer emailEventNormalizer;
    private final SmsEventNormalizer smsEventNormalizer;
    private final CourtesyEventOriginResolver courtesyEventOriginResolver;


    public void handle(SingleStatusUpdate event) {
        if (event.getDigitalLegal() != null) {
            ExtChannelOutcomeEvent legalEvent = mapLegalEvent(event.getDigitalLegal());
            channelEventProcessor.process(legalEvent, pecEventNormalizer);
        } else if (event.getDigitalCourtesy() != null) {
            CourtesyEventOriginResolver.CourtesyEventOrigin eventOrigin = courtesyEventOriginResolver.resolveOrigin(event.getDigitalCourtesy());
            switch (eventOrigin) {
                case CHANNEL_MESSAGE -> handleCourtesyChannelEvent(event.getDigitalCourtesy());
                case COURTESY_MESSAGE -> {
                    String skipMessage = "Digital courtesy event with code: {} and status: {} received for requestId: {}, but the related timeline element is a CourtesyAddressRelatedTimelineElement, skipping further actions.";
                    log.info(skipMessage, event.getDigitalCourtesy().getEventCode(), event.getDigitalCourtesy().getStatus(), event.getDigitalCourtesy().getRequestId());
                }
            }
        } else {
            throw new PnInternalException(
                    "Invalid digital event: both digitalLegal and digitalCourtesy are null",
                    ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED
            );
        }
    }

    private void handleCourtesyChannelEvent(CourtesyMessageProgressEvent event) {
        ExtChannelOutcomeEvent courtesyEvent = mapCourtesyEvent(event);
        ChannelType channelType = courtesyEvent.getEventCode().getChannelType();
        if (channelType == ChannelType.EMAIL) {
            channelEventProcessor.process(courtesyEvent, emailEventNormalizer);
        } else if (channelType == ChannelType.SMS) {
            channelEventProcessor.process(courtesyEvent, smsEventNormalizer);
        } else {
            throw new PnInternalException(
                    "Unsupported courtesy channel type: " + channelType,
                    ERROR_CODE_WORKFLOWMANAGER_INVALID_EVENT_RECEIVED
            );
        }
    }

    private ExtChannelOutcomeEvent mapLegalEvent(LegalMessageSentDetails digitalLegal) {
        ExtChannelOutcomeEvent.ExtChannelOutcomeEventBuilder builder = ExtChannelOutcomeEvent.builder()
                .requestId(digitalLegal.getRequestId())
                .eventTimestamp(digitalLegal.getEventTimestamp())
                .status(ExtChannelOutcomeStatusInt.valueOf(digitalLegal.getStatus().getValue()))
                .eventDetails(digitalLegal.getEventDetails())
                .eventCode(ExtChannelOutcomeEventCodeInt.fromValue(digitalLegal.getEventCode().getValue()));

        if(digitalLegal.getGeneratedMessage() != null) {
            builder.generatedMessage(DigitalMessageReferenceInt.builder()
                    .location(digitalLegal.getGeneratedMessage().getLocation())
                    .system(digitalLegal.getGeneratedMessage().getSystem())
                    .id(digitalLegal.getGeneratedMessage().getId())
                    .build()
            );
        }

        return builder.build();
    }

    private ExtChannelOutcomeEvent mapCourtesyEvent(CourtesyMessageProgressEvent digitalCourtesy) {
        ExtChannelOutcomeEvent.ExtChannelOutcomeEventBuilder builder = ExtChannelOutcomeEvent.builder()
                .requestId(digitalCourtesy.getRequestId())
                .eventTimestamp(digitalCourtesy.getEventTimestamp())
                .status(ExtChannelOutcomeStatusInt.valueOf(digitalCourtesy.getStatus().getValue()))
                .eventDetails(digitalCourtesy.getEventDetails())
                .eventCode(ExtChannelOutcomeEventCodeInt.fromValue(digitalCourtesy.getEventCode().getValue()));

        if(digitalCourtesy.getGeneratedMessage() != null) {
            builder.generatedMessage(DigitalMessageReferenceInt.builder()
                    .location(digitalCourtesy.getGeneratedMessage().getLocation())
                    .system(digitalCourtesy.getGeneratedMessage().getSystem())
                    .id(digitalCourtesy.getGeneratedMessage().getId())
                    .build()
            );
        }

        return builder.build();
    }
}
