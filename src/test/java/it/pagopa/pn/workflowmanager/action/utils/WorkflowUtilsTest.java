package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.dto.action.common.ActionType;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        int currentStepIdx = 1;
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);
        WorkFlowEntity secondEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-1");
        when(campaign.getWorkflow()).thenReturn(List.of(secondEntity, workflowEntity));

        when(workflowEntity.getChannel()).thenReturn(ChannelType.IO);
        when(workflowEntity.getTimeout()).thenReturn(Duration.ofMinutes(10));

        when(secondEntity.getChannel()).thenReturn(ChannelType.PEC);
        when(secondEntity.getTimeout()).thenReturn(Duration.ofMinutes(5));


        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, currentStepIdx, campaign, ChannelType.IO));
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
        int currentStepIdx = 1;
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-2");
        when(campaign.getWorkflow()).thenReturn(List.of(workflowEntity));
        when(workflowEntity.getChannel()).thenReturn(ChannelType.IO);
        when(workflowEntity.getTimeout()).thenReturn(null);

        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, currentStepIdx, campaign, ChannelType.IO));
        verifyNoInteractions(schedulerService);
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenNoWorkflowEntityMatchesChannel() {
        String iun = "IUN_123";
        int recIndex = 0;
        int currentStepIdx = 1;
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-3");
        when(campaign.getWorkflow()).thenReturn(List.of(workflowEntity));
        when(workflowEntity.getChannel()).thenReturn(ChannelType.PEC);

        assertThrows(
                PnWorkflowException.class,
                () -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, currentStepIdx, campaign, ChannelType.IO)
        );
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenWorkflowListIsNull() {
        String iun = "IUN_123";
        int recIndex = 0;
        int currentStepIdx = 1;
        Campaign campaign = mock(Campaign.class);

        when(campaign.getCampaignId()).thenReturn("campaign-5");
        when(campaign.getWorkflow()).thenReturn(null);

        assertThrows(PnWorkflowException.class, () -> workflowUtils.scheduleTimeoutForCurrentChannel(iun, recIndex, currentStepIdx, campaign, ChannelType.IO));
    }
}