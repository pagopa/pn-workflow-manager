package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.notification.informalnotification.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ChannelSenderUtilsTest {
    @Mock
    private TimelineService timelineService;

    @Mock
    private TimelineUtils timelineUtils;

    private ChannelSenderUtils channelSenderUtils;

    @BeforeEach
    void setUp() {
        channelSenderUtils = new ChannelSenderUtils(timelineService, timelineUtils);
    }

    @Test
    void shouldBuildDigitalAddressWithProvidedAddressAndType() {
        InformalDigitalAddressInt result = ChannelSenderUtils.buildDigitalAddress(
                "user@example.com",
                InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL );

        assertEquals("user@example.com", result.getAddress());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL, result.getType());
    }

    @Test
    void shouldBuildDigitalAddressWithNullAddress() {
        InformalDigitalAddressInt result = ChannelSenderUtils.buildDigitalAddress(
                null,
                InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC );

        assertNull(result.getAddress());
        assertEquals(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC, result.getType());
    }

    @Test
    void shouldBuildSendDigitalMessageEventIdFromInputValues() {
        String result = ChannelSenderUtils.buildSendDigitalMessageEventId("IUN_123",2, ChannelType.IO);

        String expected = "SEND_DIGITAL_MESSAGE.IUN_IUN_123.RECINDEX_2.CHANNEL_IO";

        assertEquals(expected, result);
    }

    @Test
    void shouldSaveSendDigitalMessageElement() {
        NotificationInt notification = mock(NotificationInt.class);
        InformalDigitalAddressInt digitalAddress = mock(InformalDigitalAddressInt.class);

        // TODO: Scommenta
        // when(timelineUtils.buildSendDigitalMessageTimelineElement(notification, "event-id", 1, digitalAddress, DigitalChannelsInt.APPIO, null))
        // .thenReturn(TimelineElementInternal.builder().build());

        assertDoesNotThrow(() -> channelSenderUtils.saveSendDigitalMessageElement(
                notification,
                "event-id",
                1,
                digitalAddress,
                DigitalChannelsInt.APPIO,
                null ));

        //TODO: Scommenta
        // verify(timelineService, times(1)).saveTimelineElement(any(), eq(notification));
    }
}