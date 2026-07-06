package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NotificationHistoryResponse;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.NotificationStatus;
import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.timelineservice.model.SendingReceipt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResponseStatusInt;
import it.pagopa.pn.workflowmanager.dto.timeline.EventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventId;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineEventIdBuilder;
import it.pagopa.pn.workflowmanager.dto.timeline.details.*;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimelineUtils {
    private final TimelineService timelineService;

    public TimelineElementInternal buildTimeline(NotificationInt notification,
                                                 TimelineElementCategoryInt category,
                                                 String elementId,
                                                 @NotNull TimelineElementDetailsInt details) {

        TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder = TimelineElementInternal.builder();

        return buildTimeline(notification, category, elementId, details, timelineBuilder);
    }

    private TimelineElementInternal buildTimeline(NotificationInt notification,
                                                  TimelineElementCategoryInt category,
                                                  String elementId,
                                                  TimelineElementDetailsInt details,
                                                  TimelineElementInternal.TimelineElementInternalBuilder timelineBuilder) {
        return timelineBuilder
                .iun(notification.getIun())
                .category(category)
                .timestamp(Instant.now())
                .elementId(elementId)
                .details(details)
                .paId(notification.getSender().getPaId())
                .notificationSentAt(notification.getSentAt())
                .build();
    }

    public TimelineElementInternal buildWorkflowEndedUndeliverableTimelineElement(Integer recIndex, NotificationInt notification,
                                                                              String eventId) {
        log.debug("buildWorkflowEndedUndeliverableTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedUndeliverableDetailsInt details = WorkflowEndedUndeliverableDetailsInt.builder()
                .recIndex(recIndex)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_UNDELIVERABLE, eventId, details);
    }

    public static String getWorkflowEndedUndeliverableTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_UNDELIVERABLE.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowEndedUnreachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                            String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowEndedUnreachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedUnreachedDetailsInt details = WorkflowEndedUnreachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_UNREACHED, eventId, details);
    }

    public static String getWorkflowEndedUnreachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_UNREACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowEndedReachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                           String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowEndedReachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowEndedReachedDetailsInt details = WorkflowEndedReachedDetailsInt.builder()
                .recIndex(recIndex)
                .notificationDate(Instant.now())
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_ENDED_REACHED, eventId, details);
    }

    public static String getWorkflowEndedReachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_ENDED_REACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowDoneUnreachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                              String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowDoneUnreachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowDoneUnreachedDetailsInt details = WorkflowDoneUnreachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_DONE_UNREACHED, eventId, details);
    }

    public static String getWorkflowDoneUnreachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_DONE_UNREACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildWorkflowDoneReachedTimelineElement(Integer recIndex, NotificationInt notification,
                                                                            String eventId, String sourceTimelineId) {
        log.debug("buildWorkflowDoneReachedTimelineElement - IUN={} and id={}", notification.getIun(), recIndex);

        WorkflowDoneReachedDetailsInt details = WorkflowDoneReachedDetailsInt.builder()
                .recIndex(recIndex)
                .sourceElementId(sourceTimelineId)
                .build();


        return buildTimeline(notification, TimelineElementCategoryInt.WORKFLOW_DONE_REACHED, eventId, details);
    }

    public static String getWorkflowDoneReachedTimelineElementId(Integer recIndex, String iun) {
        return TimelineEventId.WORKFLOW_DONE_REACHED.buildEventId(EventId.builder()
                .iun(iun)
                .recIndex(recIndex)
                .build()
        );
    }

    public TimelineElementInternal buildSendDigitalMessageTimelineElement(
            NotificationInt notification,
            String elementId,
            int recIndex,
            InformalDigitalAddressInt digitalAddress,
            DigitalChannelsInt digitalAddressChannel,
            DigitalAddressSourceInt digitalAddressSource
    ){
        log.debug("buildSendDigitalMessageTimelineElement - IUN={} and id={} and channel={}", notification.getIun(), recIndex, digitalAddressChannel);

        SendDigitalMessageDetailsInt detailsInt = SendDigitalMessageDetailsInt.builder()
                .recIndex(recIndex)
                .digitalAddress(digitalAddress)
                .channel(digitalAddressChannel)
                .digitalAddressSource(digitalAddressSource)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE, elementId, detailsInt);
    }

    public TimelineElementInternal buildDeliveredTimelineElement(
            NotificationInt notification,
            int recIndex,
            ChannelType channel,
            String sourceElementId
    ){
        log.debug("buildDeliveredTimelineElement - IUN={} and id={} and channel={}", notification.getIun(), recIndex, channel);
        String elementId = TimelineEventId.DELIVERED.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );

        DeliveredDetailsInt detailsInt = DeliveredDetailsInt.builder()
                .recIndex(recIndex)
                .channel(channel.name())
                .sourceElementId(sourceElementId)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.DELIVERED, elementId, detailsInt);
    }

    public boolean checkTimelineCategories(List<TimelineElementInternal> timelineElements,
                                           int recIndex, TimelineElementCategoryInt... categories) {
        return hasAnyTimelineCategory(timelineElements, recIndex, categories);
    }

    public Stream<TimelineElementInternal> getTimelineElementInternals(String iun) {
        Set<TimelineElementInternal> timeline = timelineService.getTimeline(iun, false);
        return timeline.stream();
    }

    private boolean hasAnyTimelineCategory(List<TimelineElementInternal> timelineElements, int recIndex,
                                           TimelineElementCategoryInt... categories) {
        return Arrays.stream(categories)
                .anyMatch(category -> isTimelineElementPresent(timelineElements, recIndex, category));
    }

    private boolean isTimelineElementPresent(List<TimelineElementInternal> timelineElements, int recIndex,
                                             TimelineElementCategoryInt category) {
        return timelineElements.stream()
                .filter(element -> category.equals(element.getCategory()))
                .map(TimelineElementInternal::getDetails)
                .filter(details -> details instanceof RecipientRelatedTimelineElementDetails)
                .map(details -> (RecipientRelatedTimelineElementDetails) details)
                .anyMatch(details -> details.getRecIndex() == recIndex);
    }

    public String retrieveCoverpageFileKey(String iun, int recIndex) {
        String timelineId = TimelineEventId.COVERPAGE_CREATION_REQUEST.buildEventId(
                EventId.builder()
                        .iun(iun)
                        .recIndex(recIndex)
                        .build()
        );

        log.debug("retrieveCoverpageFileKey - iun={} recIndex={} timelineId={}", iun, recIndex, timelineId);

        TimelineElementInternal timelineElement = timelineService.getTimelineElement(iun, timelineId)
                .orElseThrow(() -> buildTimelineElementNotPresentException(iun, recIndex, timelineId));

        if (!(timelineElement.getDetails() instanceof CoverpageCreationRequestDetailsInt details)
                || details.getFileKey() == null
                || details.getFileKey().isBlank()) {
            String msg = String.format(
                    "Timeline element %s for iun=%s recIndex=%d does not contain a valid coverpage fileKey",
                    timelineId,
                    iun,
                    recIndex
            );
            log.error(msg);
            throw new PnInternalException(msg, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }

        return details.getFileKey();
    }

    private PnInternalException buildTimelineElementNotPresentException(String iun, int recIndex, String timelineId) {
        String msg = String.format(
                "Timeline element %s not found for iun=%s recIndex=%d",
                timelineId,
                iun,
                recIndex
        );
        log.error(msg);
        return new PnInternalException(msg, ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
    }

    public void handleTransitionToReachedStatusIfNecessary(NotificationInt notificationInt, int recIndex, String sourceTimelineId) {
        String iun = notificationInt.getIun();
        int numOfRecipients = notificationInt.getRecipients().size();
        Instant sentAt = notificationInt.getSentAt();
        NotificationHistoryResponse history = timelineService.getTimelineAndStatusHistory(iun, numOfRecipients, sentAt);
        NotificationStatus currentStatus = history.getNotificationStatus();
        if(currentStatus == NotificationStatus.COMPLETED_UNREACHED) {
            log.info("Notification {} is in COMPLETED_UNREACHED status, saving element with category WORKFLOW_ENDED_REACHED for recIndex {}", iun, recIndex);
            TimelineElementInternal completedReachedTimelineElement = buildWorkflowEndedReachedTimelineElement(
                    recIndex,
                    notificationInt,
                    getWorkflowEndedReachedTimelineElementId(recIndex, iun),
                    sourceTimelineId
            );
            timelineService.addTimelineElement(completedReachedTimelineElement, notificationInt);
        }
    }

    public TimelineElementInternal buildSendDigitalMessageProgress(
            NotificationInt notification,
            int recIndex,
            DigitalChannelsInt channel,
            String requestId,
            DigitalDeliveryDetailsInt deliveryDetail,
            InformalDigitalAddressInt digitalAddress,
            DigitalAddressSourceInt digitalAddressSource,
            Instant eventTimestamp
    ) {
        log.debug("buildSendDigitalMessageProgress - IUN={} and id={} and channel={}", notification.getIun(), recIndex, channel);
        int progressIndex = timelineService.retrieveAndIncrementCounterForTimelineEvent(requestId).intValue();

        String elementId = TimelineEventId.SEND_DIGITAL_MESSAGE_PROGRESS.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .progressIndex(progressIndex)
                        .build()
        );

        SendDigitalMessageProgressDetailsInt detailsInt = SendDigitalMessageProgressDetailsInt.builder()
                .recIndex(recIndex)
                .requestId(requestId)
                .deliveryDetail(deliveryDetail)
                .digitalAddress(digitalAddress)
                .digitalAddressSource(digitalAddressSource)
                .channel(channel)
                .eventTimestamp(eventTimestamp)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_PROGRESS, elementId, detailsInt);
    }

    public TimelineElementInternal buildSendDigitalMessageFeedback(
            NotificationInt notification,
            int recIndex,
            DigitalChannelsInt channel,
            String requestId,
            DigitalDeliveryDetailsInt deliveryDetail,
            InformalDigitalAddressInt digitalAddress,
            DigitalAddressSourceInt digitalAddressSource,
            ResponseStatusInt responseStatus,
            List<SendingReceipt> sendingReceipts,
            Instant eventTimestamp
    ) {
        log.debug("buildSendDigitalMessageFeedback - IUN={} and id={} and channel={}", notification.getIun(), recIndex, channel);
        String elementId = TimelineEventId.SEND_DIGITAL_MESSAGE_FEEDBACK.buildEventId(
                EventId.builder()
                        .iun(notification.getIun())
                        .recIndex(recIndex)
                        .channel(channel.name())
                        .build()
        );

        SendDigitalMessageFeedbackDetailsInt detailsInt = SendDigitalMessageFeedbackDetailsInt.builder()
                .recIndex(recIndex)
                .requestId(requestId)
                .deliveryDetail(deliveryDetail)
                .digitalAddress(digitalAddress)
                .digitalAddressSource(digitalAddressSource)
                .responseStatus(responseStatus)
                .channel(channel)
                .notificationDate(eventTimestamp)
                .sendingReceipts(sendingReceipts)
                .build();

        return buildTimeline(notification, TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK, elementId, detailsInt);
    }

    public String getIunFromTimelineId(String timelineId) {
        //<timelineId = CATEGORY_VALUE>;IUN_<IUN_VALUE>;RECINDEX_<RECINDEX_VALUE>...
        return timelineId.split("\\" + TimelineEventIdBuilder.DELIMITER)[1].replace("IUN_", "");
    }

    public int checkIfSendRequestIsPresentAndRetrieveRecIndex(String iun, String requestId) {
        Optional<TimelineElementInternal> optRequestElement = timelineService.getTimelineElement(iun, requestId);
        if(optRequestElement.isEmpty()) {
            throw new PnInternalException(String.format("Request with requestId=%s not found in timeline for iun=%s", requestId, iun), ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }

        TimelineElementInternal requestElement = optRequestElement.get();
        if(!(requestElement.getDetails() instanceof RecipientRelatedTimelineElementDetails)) {
            throw new PnInternalException(String.format("Timeline element with requestId=%s for iun=%s is not a recipient related timeline element", requestId, iun), ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT);
        }

        return ((RecipientRelatedTimelineElementDetails) requestElement.getDetails()).getRecIndex();
    }
}
