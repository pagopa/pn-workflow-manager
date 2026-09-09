package it.pagopa.pn.workflowmanager.utils;

import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.InformalDigitalAddressRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static it.pagopa.pn.workflowmanager.action.utils.PnConstants.FIRST_ATTEMPT;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT;

@AllArgsConstructor
@Getter
@Component
@Slf4j
public class AddressSearchUtils {
    private final TimelineService timelineService;
    private final TimelineUtils timelineUtils;
    private final ChannelSenderUtils channelSenderUtils;

    public InformalDigitalAddressInt retrieveDigitalAddressFromTimeline(NotificationInt notification, int recIndex, DigitalAddressSourceInt addressSource,
                                                     DigitalChannelsInt channel, Integer attempt) {
        TimelineElementInternal timelineElementBuilder = timelineUtils.buildGetAddressTimelineElement(notification, recIndex, channel, addressSource, attempt);
        TimelineElementInternal timelineElement = timelineService.getTimelineElement(notification.getIun(), timelineElementBuilder.getElementId())
                .orElseThrow(() -> new PnNotFoundException(
                        "Timeline element not found",
                        String.format("Timeline element not found for iun=%s recIndex=%d addressSource=%s channel=%s",
                                notification.getIun(), recIndex, addressSource, channel),
                        ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT
                ));
        log.info("Timeline element found for iun={} recIndex={} addressSource={} channel={}",
                notification.getIun(), recIndex, addressSource, channel);
        InformalDigitalAddressRelatedTimelineElement details =
                (InformalDigitalAddressRelatedTimelineElement) timelineElement.getDetails();
        if (details.getDigitalAddress() == null) {
            throw new PnNotFoundException(
                    "Digital address not found",
                    String.format("Digital address not found in timeline details for iun=%s recIndex=%d addressSource=%s channel=%s",
                            notification.getIun(), recIndex, addressSource, channel),
                    ERROR_CODE_TIMELINESERVICE_TIMELINE_ELEMENT_NOT_PRESENT
            );
        }
        return details.getDigitalAddress();
    }


    public InformalDigitalAddressInt getDigitalAddress(NotificationInt notification, int recIndex,DigitalChannelsInt digitalChannelsInt,
                                                       DigitalAddressSourceInt addressSource, String timelineId) {
        if (Objects.equals(addressSource, DigitalAddressSourceInt.NONE)) {
            log.warn("Recipient digital address source is NONE - iun={} recIndex={}", notification.getIun(), recIndex);
            channelSenderUtils.saveSendDigitalMessageSkipElement(
                    recIndex,
                    notification,
                    timelineId,
                    digitalChannelsInt,
                    addressSource
            );
            return null;
        }

        return retrieveDigitalAddressFromTimeline(
                notification,
                recIndex,
                addressSource,
                digitalChannelsInt,
                FIRST_ATTEMPT
        );
    }
}
