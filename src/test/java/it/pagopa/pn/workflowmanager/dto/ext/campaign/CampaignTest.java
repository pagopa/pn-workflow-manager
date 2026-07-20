package it.pagopa.pn.workflowmanager.dto.ext.campaign;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CampaignTest {

    @Test
    void getNextChannel_shouldReturnNextChannel_whenCurrentChannelExistsAndNotLast() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        assertTrue(result.isPresent());
        Assertions.assertEquals(ChannelType.EMAIL, result.get().channel());
        Assertions.assertEquals(1, result.get().stepIndex());
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenCurrentChannelIsLast() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.SMS, RecipientTypeInt.PF
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
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.SMS, RecipientTypeInt.PF
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
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.EMAIL, RecipientTypeInt.PF
        );

        // Assert
        assertTrue(result.isPresent());
        Assertions.assertEquals(ChannelType.SMS, result.get().channel());
        Assertions.assertEquals(2, result.get().stepIndex());
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
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        assertTrue(result.isPresent());
        Assertions.assertEquals(ChannelType.SMS, result.get().channel());
        Assertions.assertEquals(1, result.get().stepIndex());
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
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PG
        );

        // Assert
        assertTrue(result.isPresent());
        Assertions.assertEquals(ChannelType.SMS, result.get().channel());
        Assertions.assertEquals(1, result.get().stepIndex());
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
        Optional<NextChannel> resultPF = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        assertTrue(resultPF.isPresent());
        Assertions.assertEquals(ChannelType.EMAIL, resultPF.get().channel());
        Assertions.assertEquals(1, resultPF.get().stepIndex());
    }

    @Test
    void getNextChannel_shouldReturnEmpty_whenWorkflowHasOnlyOneChannel() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO)
        );

        // Act
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void getNextChannel_shouldThrowException_whenWorkflowIsEmpty() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .workflow(List.of())
                .build();

        // Act & Assert
        Assertions.assertThrows(PnWorkflowException.class, () -> campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        ));
    }

    @Test
    void getNextChannel_shouldThrowException_whenNoStepsForRecipientType() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL)
        );

        // Act & Assert
        Assertions.assertThrows(PnWorkflowException.class, () -> campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PG
        ));
    }

    @Test
    void getNextChannel_shouldHandleFirstChannel() {
        // Arrange
        Campaign campaign = createCampaignWithMultipleChannels(
                List.of(ChannelType.IO, ChannelType.EMAIL, ChannelType.SMS)
        );

        // Act
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.IO, RecipientTypeInt.PF
        );

        // Assert
        assertTrue(result.isPresent());
        Assertions.assertEquals(ChannelType.EMAIL, result.get().channel());
        Assertions.assertEquals(1, result.get().stepIndex());
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
        Optional<NextChannel> result = campaign.getNextChannel(
                ChannelType.EMAIL, RecipientTypeInt.PF
        );

        // Assert
        Assertions.assertFalse(result.isPresent());
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