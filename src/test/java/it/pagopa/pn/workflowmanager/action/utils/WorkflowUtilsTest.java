package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class WorkflowUtilsTest {

    private WorkflowUtils workflowUtils;

    @BeforeEach
    void setup() {
        workflowUtils = new WorkflowUtils();
    }

    @Test
    void getNextChannel_shouldReturnNextChannel_whenCurrentChannelExistsAndNotLast() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.EMAIL, result.get().channel()),
                () -> Assertions.assertEquals(1, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenCurrentChannelIsLast() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.SMS, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenCurrentChannelNotFound() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.SMS, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldReturnNextChannel_whenMultipleStepsExist() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS, ChannelType.ANALOG)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.EMAIL, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.SMS, result.get().channel()),
                () -> Assertions.assertEquals(2, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldFilterByRecipientType_PF() {
        // Arrange
        List<WorkFlowEntity> workflow = List.of(
                createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PG)),
                createWorkflowEntity(ChannelType.SMS, Set.of(RecipientTypeInt.PF))
        );
        Campaign campaign = Campaign.builder().workflow(workflow).build();

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.SMS, result.get().channel()),
                () -> Assertions.assertEquals(1, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldFilterByRecipientType_PG() {
        // Arrange
        List<WorkFlowEntity> workflow = List.of(
                createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PG)),
                createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.SMS, Set.of(RecipientTypeInt.PG))
        );
        Campaign campaign = Campaign.builder().workflow(workflow).build();

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PG
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.SMS, result.get().channel()),
                () -> Assertions.assertEquals(1, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldHandleMixedRecipientTypes() {
        // Arrange
        List<WorkFlowEntity> workflow = List.of(
                createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF, RecipientTypeInt.PG)),
                createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PF, RecipientTypeInt.PG)),
                createWorkflowEntity(ChannelType.SMS, Set.of(RecipientTypeInt.PF))
        );
        Campaign campaign = Campaign.builder().workflow(workflow).build();

        // Act - Test with PF
        Optional<WorkflowUtils.NextChannel> resultPF = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(resultPF.isPresent()),
                () -> Assertions.assertEquals(ChannelType.EMAIL, resultPF.get().channel()),
                () -> Assertions.assertEquals(1, resultPF.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenNoStepsForRecipientType() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PG
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenWorkflowHasOnlyOneChannel() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenWorkflowIsEmpty() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .workflow(List.of())
                .build();

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldHandleFirstChannel() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.EMAIL, result.get().channel()),
                () -> Assertions.assertEquals(1, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldHandleMiddleChannel() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.EMAIL, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.SMS, result.get().channel()),
                () -> Assertions.assertEquals(2, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldReturnCorrectStepIndex() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS, ChannelType.ANALOG)
        );

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.SMS, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.ANALOG, result.get().channel()),
                () -> Assertions.assertEquals(3, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldHandleComplexWorkflow() {
        // Arrange
        List<WorkFlowEntity> workflow = List.of(
                createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PG)),
                createWorkflowEntity(ChannelType.SMS, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.ANALOG, Set.of(RecipientTypeInt.PF, RecipientTypeInt.PG))
        );
        Campaign campaign = Campaign.builder().workflow(workflow).build();

        // Act - Find next after SMS for PF
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.SMS, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.isPresent()),
                () -> Assertions.assertEquals(ChannelType.ANALOG, result.get().channel()),
                () -> Assertions.assertEquals(2, result.get().stepIndex())
        );
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenCurrentChannelIsLastForRecipientType() {
        // Arrange
        List<WorkFlowEntity> workflow = List.of(
                createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PF)),
                createWorkflowEntity(ChannelType.SMS, Set.of(RecipientTypeInt.PG))
        );
        Campaign campaign = Campaign.builder().workflow(workflow).build();

        // Act
        Optional<WorkflowUtils.NextChannel> result = workflowUtils.getNextChannel(
                campaign, ChannelType.EMAIL, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void nextChannel_record_shouldStoreCorrectValues() {
        // Act
        WorkflowUtils.NextChannel nextChannel = new WorkflowUtils.NextChannel(ChannelType.EMAIL, 5);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(ChannelType.EMAIL, nextChannel.channel()),
                () -> Assertions.assertEquals(5, nextChannel.stepIndex())
        );
    }

    private Campaign createCampaignWithMultipleChannels(List<ChannelType> channels) {
        List<WorkFlowEntity> workflow = channels.stream()
                .map(channel -> createWorkflowEntity(channel, Set.of(RecipientTypeInt.PF)))
                .toList();

        return Campaign.builder()
                .workflow(workflow)
                .build();
    }

    private WorkFlowEntity createWorkflowEntity(ChannelType channel, Set<RecipientTypeInt> recipientTypes) {
        return WorkFlowEntity.builder()
                .channel(channel)
                .recipientType(recipientTypes)
                .build();
    }
}
