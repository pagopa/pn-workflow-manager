package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.utils.SendAttachmentMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ChannelSenderUtilsTest {
    private TimelineService timelineService;

    private TimelineUtils timelineUtils;

    private AttachmentUtils attachmentUtils;

    private ChannelSenderUtils channelSenderUtils;

    private static final String IUN = "IUN_TEST_123";
    private static final int REC_INDEX = 0;

    @BeforeEach
    void setUp() {
        timelineService = mock(TimelineService.class);
        timelineUtils = mock(TimelineUtils.class);
        attachmentUtils = mock(AttachmentUtils.class);
        channelSenderUtils = new ChannelSenderUtils(timelineService, timelineUtils, attachmentUtils);
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

    @Test
    void resolveAttachmentsForChannelShouldRetrieveAttachmentsWhenChannelRequires() {
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        ChannelType channelType = ChannelType.EMAIL;
        // build campaign that has workflow with channel that requires attachments
        WorkFlowEntity workflowEntity = WorkFlowEntity.builder()
                .channel(channelType)
                .includeAttachment(true)
                .build();
        Campaign campaign = Campaign.builder()
                .workflow(List.of(workflowEntity))
                .build();
        SendAttachmentMode sendAttachmentMode = mock(SendAttachmentMode.class);

        when(attachmentUtils.retrieveAttachmentTypesToSend(notification, channelType)).thenReturn(sendAttachmentMode);
        when(attachmentUtils.retrieveAttachments(notification, recIndex, sendAttachmentMode, false)).thenReturn(List.of("attachment1", "attachment2"));

        List<String> attachments = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, channelType);

        verify(attachmentUtils).retrieveAttachmentTypesToSend(notification, channelType);
        verify(attachmentUtils).retrieveAttachments(notification, recIndex, sendAttachmentMode, false);
        assertEquals(List.of("attachment1", "attachment2"), attachments);
    }

    @Test
    void resolveAttachmentsForChannelShouldThrowExceptionWhenWorkflowStepNotFound() {
        // Arrange
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        ChannelType channelType = ChannelType.EMAIL;

        Campaign campaign = Campaign.builder()
                .campaignId("CAMP-123")
                .workflow(List.of()) // Workflow vuoto
                .build();

        // Act & Assert
        assertThrows(PnWorkflowException.class, () -> channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, channelType));

        verifyNoInteractions(attachmentUtils);
    }

    @Test
    void resolveAttachmentsForChannelShouldThrowExceptionWhenChannelMismatch() {
        // Arrange
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        ChannelType requestedChannel = ChannelType.EMAIL;
        ChannelType workflowChannel = ChannelType.SMS; // Canale differente

        WorkFlowEntity workflowEntity = WorkFlowEntity.builder()
                .channel(workflowChannel)
                .build();

        Campaign campaign = Campaign.builder()
                .campaignId("CAMP-456")
                .workflow(List.of(workflowEntity))
                .build();

        // Act & Assert
        assertThrows(PnWorkflowException.class, () -> channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, requestedChannel));

        verifyNoInteractions(attachmentUtils);
    }

    @Test
    void resolveAttachmentsForChannelShouldReturnEmptyListWhenAttachmentNotRequired() {
        // Arrange
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        ChannelType channelType = ChannelType.EMAIL;

        WorkFlowEntity workflowEntity = WorkFlowEntity.builder()
                .channel(channelType)
                .includeAttachment(false) // Allegati disabilitati
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workflowEntity))
                .build();

        // Act
        List<String> attachments = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, channelType);

        // Assert
        assertTrue(attachments.isEmpty());
        assertEquals(List.of(), attachments);
        verifyNoInteractions(attachmentUtils);
    }

    @Test
    void resolveAttachmentsForChannelShouldReturnEmptyListWhenIncludeAttachmentIsNull() {
        // Arrange
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        ChannelType channelType = ChannelType.EMAIL;

        WorkFlowEntity workflowEntity = WorkFlowEntity.builder()
                .channel(channelType)
                .includeAttachment(null) // Flag Nullo
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workflowEntity))
                .build();

        // Act
        List<String> attachments = channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, campaign, channelType);

        // Assert
        Assertions.assertTrue(attachments.isEmpty());
        verifyNoInteractions(attachmentUtils);
    }
}