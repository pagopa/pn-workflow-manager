package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowUtilsTest {
    private WorkflowUtils workflowUtils;

    @BeforeEach
    void setUp() {
        workflowUtils = new WorkflowUtils();
    }

    @Test
    void shouldScheduleWithoutErrorsWhenTimeoutIsDefinedForCurrentChannel() {
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);
        WorkFlowEntity secondEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-1");
        when(campaign.getWorkflow()).thenReturn(List.of(secondEntity, workflowEntity));

        when(workflowEntity.getChannel()).thenReturn(ChannelType.IO);
        when(workflowEntity.getTimeout()).thenReturn(Duration.ofMinutes(10));

        when(secondEntity.getChannel()).thenReturn(ChannelType.PEC);
        when(secondEntity.getTimeout()).thenReturn(Duration.ofMinutes(5));


        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(campaign, ChannelType.IO));
    }

    @Test
    void shouldNotThrowWhenTimeoutIsNullForCurrentChannel() {
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-2");
        when(campaign.getWorkflow()).thenReturn(List.of(workflowEntity));
        when(workflowEntity.getChannel()).thenReturn(ChannelType.IO);
        when(workflowEntity.getTimeout()).thenReturn(null);

        assertDoesNotThrow(() -> workflowUtils.scheduleTimeoutForCurrentChannel(campaign, ChannelType.IO));
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenNoWorkflowEntityMatchesChannel() {
        Campaign campaign = mock(Campaign.class);
        WorkFlowEntity workflowEntity = mock(WorkFlowEntity.class);

        when(campaign.getCampaignId()).thenReturn("campaign-3");
        when(campaign.getWorkflow()).thenReturn(List.of(workflowEntity));
        when(workflowEntity.getChannel()).thenReturn(ChannelType.PEC);

        assertThrows(
                PnWorkflowException.class,
                () -> workflowUtils.scheduleTimeoutForCurrentChannel(campaign, ChannelType.IO)
        );
    }

    @Test
    void shouldThrowPnWorkflowExceptionWhenWorkflowListIsNull() {
        Campaign campaign = mock(Campaign.class);

        when(campaign.getCampaignId()).thenReturn("campaign-5");
        when(campaign.getWorkflow()).thenReturn(null);

        assertThrows(PnWorkflowException.class, () -> workflowUtils.scheduleTimeoutForCurrentChannel(campaign, ChannelType.IO));
    }
}