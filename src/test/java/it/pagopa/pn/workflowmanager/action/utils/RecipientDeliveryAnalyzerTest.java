package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageFeedbackDetailsInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendDigitalMessageSkipDetailsInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_FEEDBACK;
import static it.pagopa.pn.workflowmanager.dto.timeline.details.TimelineElementCategoryInt.SEND_DIGITAL_MESSAGE_SKIP;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_GENERIC_WORKFLOW_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientDeliveryAnalyzerTest {

    @Mock
    private TimelineUtils timelineUtils;

    private RecipientDeliveryAnalyzer analyzer;
    private static final int TEST_REC_INDEX = 0;

    @BeforeEach
    void setup() {
        analyzer = new RecipientDeliveryAnalyzer(timelineUtils);
    }

    @Test
    void getDeliveryInfo_shouldReturnReached_whenRecipientHasBeenReached() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.of(TimelineElementInternal.builder().elementId("reached_id").build()));

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.REACHED, result.status());
        assertEquals("reached_id", result.sourceElementId());
        verify(timelineUtils).findFirstReachedTimelineElement(eq(timelineElements), eq(TEST_REC_INDEX));
    }

    @Test
    void getDeliveryInfo_shouldReturnUndeliverable_whenAllChannelsHaveSkipOrFeedback() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS));
        List<TimelineElementInternal> timelineElements = createTimelineWithAllChannelsFailed();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result.status());
        assertNull(result.sourceElementId());
    }

    @Test
    void getDeliveryInfo_shouldReturnUndeliverable_whenOnlyIoChannelAndHasFeedback() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO));
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result.status());
        assertNull(result.sourceElementId());
    }

    @Test
    void getDeliveryInfo_shouldReturnUndeliverable_whenOnlyEmailChannelAndHasSkip() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.EMAIL));
        List<TimelineElementInternal> timelineElements = createTimelineWithEmailSkip();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result.status());
        assertNull(result.sourceElementId());
    }

    @Test
    void getDeliveryInfo_shouldReturnUndeliverable_whenOnlySmsChannelAndHasSkip() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.SMS));
        List<TimelineElementInternal> timelineElements = createTimelineWithSmsSkip();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result.status());
        assertNull(result.sourceElementId());
    }

    @Test
    void getDeliveryInfo_shouldHandlePgRecipientType() {
        // Arrange
        Campaign campaign = createCampaignForRecipientType(List.of(ChannelType.IO), RecipientTypeInt.PG);
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PG);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNDELIVERABLE, result.status());
        assertNull(result.sourceElementId());
    }

    @Test
    void getDeliveryInfo_shouldThrowPnWorkflowError_whenNoChannelsConfigured() {
        // Arrange
        Campaign campaign = createCampaign(List.of());
        List<TimelineElementInternal> timelineElements = List.of();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        PnWorkflowException exception = assertThrows(
                PnWorkflowException.class,
                () -> analyzer.getDeliveryInfo(timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF)
        );

        // Assert
        assertEquals(ERROR_CODE_WORKFLOWMANAGER_GENERIC_WORKFLOW_ERROR,
                exception.getProblem().getErrors().getFirst().getCode());
    }

    @Test
    void getDeliveryInfo_shouldReturnUnreached_whenNoChannelConditionsMet() {
        // Arrange
        Campaign campaign = createCampaign(List.of(ChannelType.IO, ChannelType.EMAIL));
        List<TimelineElementInternal> timelineElements = createTimelineWithAppIoFeedback();

        when(timelineUtils.findFirstReachedTimelineElement(anyList(), eq(TEST_REC_INDEX)))
                .thenReturn(Optional.empty());

        // Act
        RecipientDeliveryInfo result = analyzer.getDeliveryInfo(
                timelineElements, campaign, TEST_REC_INDEX, RecipientTypeInt.PF);

        // Assert
        assertEquals(RecipientDeliveryStatus.UNREACHED, result.status());
        assertNull(result.sourceElementId());
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
        timeline.addAll(createTimelineWithAppIoFeedback());
        timeline.addAll(createTimelineWithEmailSkip());
        timeline.addAll(createTimelineWithSmsSkip());
        return timeline;
    }

    private List<TimelineElementInternal> createTimelineWithAppIoFeedback() {
        SendDigitalMessageFeedbackDetailsInt details = SendDigitalMessageFeedbackDetailsInt.builder()
                .recIndex(TEST_REC_INDEX)
                .channel(DigitalChannelsInt.IO)
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
