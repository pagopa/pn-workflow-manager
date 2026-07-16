package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.dto.action.details.NotHandledDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.action.details.WorkflowDoneDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowUtilsTest {

    private SchedulerService schedulerService;
    private WorkflowUtils workflowUtils;

    @BeforeEach
    void setUp() {
        this.schedulerService = mock(SchedulerService.class);
        workflowUtils = new WorkflowUtils(schedulerService);
    }

    @Test
    void shouldScheduleWithoutErrorsWhenTimeoutIsDefinedForCurrentChannel() {
        String iun = "IUN_123";
        int recIndex = 0;
        WorkFlowEntity workflowEntity = createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF));
        workflowEntity.setTimeout(Duration.ofMinutes(10));
        WorkFlowEntity secondEntity = createWorkflowEntity(ChannelType.PEC, Set.of(RecipientTypeInt.PF));

        Campaign campaign = Campaign.builder()
                .workflow(List.of(secondEntity, workflowEntity))
                .build();


        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, campaign, ChannelType.IO));
        verify(schedulerService).scheduleEvent(
                eq(iun),
                eq(recIndex),
                any(),
                eq(ActionType.TIMEOUT_WORKFLOW),
                any()
        );
    }

    @Test
    void shouldNotThrowWhenTimeoutIsNullForCurrentChannel() {
        String iun = "IUN_123";
        int recIndex = 0;
        Campaign campaign = Campaign.builder()
                .workflow(List.of(createWorkflowEntity(ChannelType.IO, Set.of(RecipientTypeInt.PF))))
                .build();

        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, campaign, ChannelType.IO));
        verifyNoInteractions(schedulerService);
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenNoWorkflowEntityMatchesChannel() {
        String iun = "IUN_123";
        int recIndex = 0;
        WorkFlowEntity workflowEntity = createWorkflowEntity(ChannelType.PEC, Set.of(RecipientTypeInt.PF));
        Campaign campaign = Campaign.builder()
                .workflow(List.of(workflowEntity))
                .build();

        assertThrows(
                PnWorkflowException.class,
                () -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, campaign, ChannelType.IO)
        );
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenWorkflowListIsNull() {
        String iun = "IUN_123";
        int recIndex = 0;

        Campaign campaign = Campaign.builder()
                .workflow(null)
                .build();

        assertThrows(PnWorkflowException.class, () -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, campaign, ChannelType.IO));
    }

    @Test
    void isDesiredFeedbackShouldReturnTrueWhenWorkflowAndFeedbackMatch() {
        // Arrange
        ChannelType channel = ChannelType.PEC;
        DesiredFeedbackType desiredFeedback = DesiredFeedbackType.RECEIVED;

        WorkFlowEntity workFlow = WorkFlowEntity.builder()
                .desiredFeedback(Set.of(desiredFeedback))
                .channel(channel)
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workFlow))
                .build();

        // Act
        boolean result = workflowUtils.isDesiredFeedback(campaign, channel, desiredFeedback);

        // Assert
        assertTrue(result);
    }

    @Test
    void isDesiredFeedbackShouldReturnFalseWhenChannelMatchesButFeedbackDiffers() {
        // Arrange
        ChannelType channel = ChannelType.PEC;
        DesiredFeedbackType desiredFeedback = DesiredFeedbackType.RECEIVED;

        WorkFlowEntity workFlow = WorkFlowEntity.builder()
                .desiredFeedback(Set.of(desiredFeedback))
                .channel(channel)
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workFlow))
                .build();

        // Act
        boolean result = workflowUtils.isDesiredFeedback(campaign, channel, DesiredFeedbackType.PAID);

        // Assert
        assertFalse(result);
    }

    @Test
    void isDesiredFeedbackShouldThrowPnWorkflowExceptionWhenWorkflowHasDifferentChannel() {
        // Arrange
        DesiredFeedbackType desiredFeedback = DesiredFeedbackType.RECEIVED;

        WorkFlowEntity workFlow = WorkFlowEntity.builder()
                .desiredFeedback(Set.of(desiredFeedback))
                .channel(ChannelType.EMAIL)
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workFlow))
                .build();

        // Act
        assertThrows(PnWorkflowException.class, () -> workflowUtils.isDesiredFeedback(campaign, ChannelType.IO, DesiredFeedbackType.RECEIVED));
    }

    @Test
    void isDesiredFeedbackShouldThrowsPnWorkflowExceptionWhenWorkflowIsEmpty() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .workflow(List.of())
                .build();

        // Act
        assertThrows(PnWorkflowException.class, () -> workflowUtils.isDesiredFeedback(campaign, ChannelType.PEC, DesiredFeedbackType.RECEIVED));

    }

    @Test
    void isDesiredFeedbackShouldReturnFalseWhenWorkflowDoesNotHaveDesiredFeedbacks() {
        // EMPTY desiredFeedback set
        // Arrange
        WorkFlowEntity workFlow = WorkFlowEntity.builder()
                .desiredFeedback(Set.of())
                .channel(ChannelType.PEC)
                .build();

        Campaign campaign = Campaign.builder()
                .workflow(List.of(workFlow))
                .build();

        // Act
        boolean result = workflowUtils.isDesiredFeedback(campaign, ChannelType.PEC, DesiredFeedbackType.RECEIVED);

        // Assert
        assertFalse(result);

        // NULL desiredFeedback set
        workFlow.setDesiredFeedback(null);

        // Act
        boolean result2 = workflowUtils.isDesiredFeedback(campaign, ChannelType.PEC, DesiredFeedbackType.RECEIVED);

        // Assert
        assertFalse(result2);

    }

    @Test
    void advanceWorkflowShouldScheduleEndWorkflowWhenNoNextChannelFound() {
        // Arrange
        Campaign campaign = Campaign.builder()
                .workflow(List.of(createWorkflowEntity(ChannelType.EMAIL, Set.of(RecipientTypeInt.PF))))
                .build();
        String iun = "IUN_123";
        int recIndex = 0;
        ChannelType currentChannel = ChannelType.EMAIL;
        RecipientTypeInt recipientType = RecipientTypeInt.PF;

        // Act
        workflowUtils.advanceWorkflow(iun, recIndex, currentChannel, campaign, recipientType);

        // Assert
        verify(schedulerService).scheduleEvent(
                eq(iun),
                eq(recIndex),
                any(Instant.class),
                eq(ActionType.END_WORKFLOW),
                any(NotHandledDetails.class)
        );

        // Verifichiamo che non sia stato chiamato lo start del canale successivo
        verify(schedulerService, never()).scheduleEvent(
                any(), anyInt(), any(), eq(ActionType.START_WORKFLOW), any()
        );
    }

    @Test
    void advanceWorkflowShouldScheduleNextChannelWhenNextChannelExists() {
        Campaign campaign = createCampaignWithMultipleChannels(List.of(ChannelType.IO, ChannelType.EMAIL));
        String iun = "IUN_123";
        int recIndex = 0;
        ChannelType currentChannel = ChannelType.IO;
        RecipientTypeInt recipientType = RecipientTypeInt.PF;

        // Act
        workflowUtils.advanceWorkflow(iun, recIndex, currentChannel, campaign, recipientType);

        // Assert
        // Costruiamo il dettaglio atteso per verificare il Builder
        StartWorkflowDetails expectedDetails = StartWorkflowDetails.builder()
                .stepIdx(1)
                .channel(ChannelType.EMAIL)
                .build();

        verify(schedulerService).scheduleEvent(
                eq(iun),
                eq(recIndex),
                any(Instant.class),
                eq(ActionType.START_WORKFLOW),
                eq(expectedDetails)
        );

        // Verifichiamo che non sia stato chiamato l'end workflow
        verify(schedulerService, never()).scheduleEvent(
                any(), anyInt(), any(), eq(ActionType.END_WORKFLOW), any()
        );
    }

    @Test
    void scheduleWorkflowDoneShouldScheduleWorkflowDoneEvent() {
        String iun = "IUN_123";
        int recIndex = 0;
        String elementId = "ELEMENT_456";

        // Act
        workflowUtils.scheduleWorkflowDone(iun, recIndex, elementId, DesiredFeedbackType.SENT);

        // Assert
        ArgumentCaptor<WorkflowDoneDetails> workflowDoneDetailsArgumentCaptor = ArgumentCaptor.forClass(WorkflowDoneDetails.class);
        verify(schedulerService).scheduleEvent(
                eq(iun),
                eq(recIndex),
                any(Instant.class),
                eq(ActionType.WORKFLOW_DONE),
                eq(elementId),
                workflowDoneDetailsArgumentCaptor.capture()
        );
        assertEquals(DesiredFeedbackType.SENT, workflowDoneDetailsArgumentCaptor.getValue().getCompletionFeedback());
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
