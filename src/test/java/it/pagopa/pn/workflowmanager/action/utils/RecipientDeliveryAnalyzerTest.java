package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageFeedbackDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageSkipDetailsInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientDeliveryAnalyzerTest {

    @Mock
    private TimelineUtils timelineUtils;

    private RecipientDeliveryAnalyzer analyzer;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;

    @BeforeEach
    void setup() {
        analyzer = new RecipientDeliveryAnalyzer(timelineUtils);
    }

    @Test
    void getDeliveryStatus_shouldReturnReached_whenRecipientHasDeliveredEvent() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(true);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                new ArrayList<>(), campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.REACHED, result);
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);
    }

    @Test
    void getDeliveryStatus_shouldReturnReached_whenRecipientHasViewedEvent() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(true);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                new ArrayList<>(), campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.REACHED, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUndeliverable_whenAllChannelsHaveSkipOrFeedback() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS));

        List<TimelineElementInternal> timelineElements = createTimelineWithAllChannelsFailed();

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUnreached_whenSomeChannelsStillPending() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO, ChannelType.EMAIL));

        // Only AppIO has feedback, EMAIL doesn't have skip yet
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback(TEST_REC_INDEX);

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNREACHED, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUndeliverable_whenOnlyIoChannelAndHasFeedback() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback(TEST_REC_INDEX);

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUndeliverable_whenOnlyEmailChannelAndHasSkip() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.EMAIL));
        List<TimelineElementInternal> timelineElements = createTimelineWithEmailSkip();

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUndeliverable_whenOnlySmsChannelAndHasSkip() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.SMS));
        List<TimelineElementInternal> timelineElements = createTimelineWithSmsSkip();

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldHandlePgRecipientType() {
        // Arrange
        Campaign campaign = createCampaignForRecipientType(List.of(ChannelType.IO), RecipientTypeInt.PG);
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback(TEST_REC_INDEX);

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PG, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldReturnUndeliverable_whenNoChannelsConfigured() {
        // Arrange
        Campaign campaign = createCampaign(List.of());

        when(timelineUtils.checkTimelineCategories(TEST_IUN, TEST_REC_INDEX, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                new ArrayList<>(), campaign, TEST_REC_INDEX, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
    }

    @Test
    void getDeliveryStatus_shouldHandleMultipleRecipientIndices() {
        // Arrange
        int recIndex2 = 2;
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback(recIndex2);

        when(timelineUtils.checkTimelineCategories(TEST_IUN, recIndex2, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT))
                .thenReturn(false);

        // Act
        RecipientDeliveryStatus result = analyzer.getDeliveryStatus(
                timelineElements, campaign, recIndex2, RecipientTypeInt.PF, TEST_IUN);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result);
        verify(timelineUtils).checkTimelineCategories(TEST_IUN, recIndex2, DELIVERED, INFORMAL_NOTIFICATION_VIEWED, PAYMENT);
    }

    private Campaign createCampaign(List<ChannelType> channels) {
        return createCampaignForRecipientType(channels, RecipientTypeInt.PF);
    }

    private Campaign createCampaignForRecipientType(List<ChannelType> channels, RecipientTypeInt recipientType) {
        List<WorkFlowEntity> workflow = channels.stream()
                .map(channel -> WorkFlowEntity.builder()
                        .channel(channel)
                        .recipientType(Set.of(recipientType))
                        .build())
                .toList();

        return Campaign.builder()
                .workflow(workflow)
                .build();
    }

    private List<TimelineElementInternal> createTimelineWithAllChannelsFailed() {
        List<TimelineElementInternal> timeline = new ArrayList<>();
        timeline.addAll(createTimelineWithAppIoFeedback(RecipientDeliveryAnalyzerTest.TEST_REC_INDEX));
        timeline.addAll(createTimelineWithEmailSkip());
        timeline.addAll(createTimelineWithSmsSkip());
        return timeline;
    }

    private List<TimelineElementInternal> createTimelineWithAppIoFeedback(int recIndex) {
        SendDigitalMessageFeedbackDetailsInt details = SendDigitalMessageFeedbackDetailsInt.builder()
                .recIndex(recIndex)
                .channel(DigitalChannelsInt.APPIO)
                .build();

        TimelineElementInternal element = TimelineElementInternal.builder()
                .category(SEND_DIGITAL_MESSAGE_FEEDBACK)
                .details(details)
                .build();

        return List.of(element);
    }

    private List<TimelineElementInternal> createTimelineWithEmailSkip() {
        SendDigitalMessageSkipDetailsInt details = SendDigitalMessageSkipDetailsInt.builder()
                .recIndex(RecipientDeliveryAnalyzerTest.TEST_REC_INDEX)
                .channel(DigitalChannelsInt.EMAIL)
                .build();

        TimelineElementInternal element = TimelineElementInternal.builder()
                .category(SEND_DIGITAL_MESSAGE_SKIP)
                .details(details)
                .build();

        return List.of(element);
    }

    private List<TimelineElementInternal> createTimelineWithSmsSkip() {
        SendDigitalMessageSkipDetailsInt details = SendDigitalMessageSkipDetailsInt.builder()
                .recIndex(RecipientDeliveryAnalyzerTest.TEST_REC_INDEX)
                .channel(DigitalChannelsInt.SMS)
                .build();

        TimelineElementInternal element = TimelineElementInternal.builder()
                .category(SEND_DIGITAL_MESSAGE_SKIP)
                .details(details)
                .build();

        return List.of(element);
    }
}
