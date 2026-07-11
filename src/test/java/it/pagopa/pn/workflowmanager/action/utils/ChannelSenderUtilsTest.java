package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChannelSenderUtilsTest {
    private TimelineService timelineService;

    private TimelineUtils timelineUtils;

    private ChannelSenderUtils channelSenderUtils;

    private static final String IUN = "IUN_TEST_123";
    private static final int REC_INDEX = 0;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);
        timelineUtils = mock(TimelineUtils.class);
        channelSenderUtils = new ChannelSenderUtils(timelineService, timelineUtils);
    }

    @Test
    void shouldBuildDigitalAddressWithProvidedAddressAndType() {
        InformalDigitalAddressInt result = ChannelSenderUtils.buildDigitalAddress(
                "user@example.com",
                InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL);

        assertEquals("user@example.com", result.getAddress());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL, result.getType());
    }

    @Test
    void shouldBuildSendDigitalMessageEventIdFromInputValues() {
        String result = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, REC_INDEX, ChannelType.EMAIL);

        assertEquals("SEND_DIGITAL_MESSAGE.IUN_IUN_TEST_123.RECINDEX_0.CHANNEL_EMAIL", result);
    }

    @Test
    void shouldBuildSendDigitalMessageSkipTimelineElementId() {
        String result = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(REC_INDEX, IUN, ChannelType.EMAIL);

        assertEquals("SEND_DIGITAL_MESSAGE_SKIP.IUN_IUN_TEST_123.RECINDEX_0.CHANNEL_EMAIL", result);
    }

    @Test
    void shouldBuildPrepareAnalogDeliveryTimelineElementId() {
        String result = ChannelSenderUtils.buildPrepareAnalogDeliveryTimelineElementId(REC_INDEX, IUN, 0);

        assertEquals("PREPARE_ANALOG_DELIVERY.IUN_IUN_TEST_123.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS", result);
    }

    @Test
    void shouldSaveSendDigitalMessageElement() {
        NotificationInt notification = mock(NotificationInt.class);
        InformalDigitalAddressInt digitalAddress = mock(InformalDigitalAddressInt.class);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(timelineUtils.buildSendDigitalMessageTimelineElement(
                notification, "event-id", REC_INDEX, digitalAddress, DigitalChannelsInt.EMAIL, null))
                .thenReturn(timelineElement);

        channelSenderUtils.saveSendDigitalMessageElement(
                notification, "event-id", REC_INDEX,
                digitalAddress, DigitalChannelsInt.EMAIL, null);

        verify(timelineUtils).buildSendDigitalMessageTimelineElement(
                notification, "event-id", REC_INDEX, digitalAddress, DigitalChannelsInt.EMAIL, null);
        verify(timelineService).addTimelineElement(timelineElement, notification);
    }

    @Test
    void shouldSaveSendDigitalMessageSkipElement() {
        NotificationInt notification = mock(NotificationInt.class);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(timelineUtils.buildSendDigitalMessageSkipTimelineElement(
                REC_INDEX, notification, "skip-event-id", DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL))
                .thenReturn(timelineElement);

        channelSenderUtils.saveSendDigitalMessageSkipElement(
                REC_INDEX, notification, "skip-event-id",
                DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL);

        verify(timelineUtils).buildSendDigitalMessageSkipTimelineElement(
                REC_INDEX, notification, "skip-event-id", DigitalChannelsInt.EMAIL, DigitalAddressSourceInt.SPECIAL);
        verify(timelineService).addTimelineElement(timelineElement, notification);
    }

    @Test
    void shouldSavePrepareAnalogDeliveryElement() {
        NotificationInt notification = mock(NotificationInt.class);
        PhysicalAddressInt physicalAddressInt = mock(PhysicalAddressInt.class);
        TimelineElementInternal timelineElement = TimelineElementInternal.builder().build();

        when(timelineUtils.buildPrepareAnalogDeliveryTimelineElement(
                REC_INDEX, notification, "prepare-event-id", null, 0,null,physicalAddressInt))
                .thenReturn(timelineElement);

        channelSenderUtils.savePrepareAnalogDeliveryElement(
                REC_INDEX, notification, "prepare-event-id", null, 0,null,physicalAddressInt);

        verify(timelineUtils).buildPrepareAnalogDeliveryTimelineElement(
                REC_INDEX, notification, "prepare-event-id", null, 0,null,physicalAddressInt);
        verify(timelineService).addTimelineElement(timelineElement, notification);
    }

}