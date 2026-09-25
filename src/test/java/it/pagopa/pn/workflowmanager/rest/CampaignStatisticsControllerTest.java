package it.pagopa.pn.workflowmanager.rest;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.config.springbootcfg.PnResponseEntityExceptionHandlerActivation;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignNotFoundException;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignStatisticsNotFoundException;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStats;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_NOT_FOUND;
import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignStatisticsController.class)
@Import(PnResponseEntityExceptionHandlerActivation.class)
class CampaignStatisticsControllerTest {

    private static final String SENDER_ID = "sender-001";
    private static final String CAMPAIGN_ID = "campaign-001";
    private static final String PATH = "/workflow-private/informal/{campaignId}/statistics";
    private static final String SENDER_HEADER = "x-pagopa-pn-cx-id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignStatisticsService service;

    @MockitoBean
    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;

    @Test
    void getCampaignStatistics() throws Exception {
        CampaignStatisticsResponse response = new CampaignStatisticsResponse()
                .campaignId(CAMPAIGN_ID)
                .stats(new CampaignStats().totalCount(12))
                .lastCompletedTimestamp(Instant.parse("2026-06-08T19:13:00Z"));
        when(service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID)).thenReturn(response);

        mockMvc.perform(get(PATH, CAMPAIGN_ID)
                        .header(SENDER_HEADER, SENDER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaignId").value(CAMPAIGN_ID))
                .andExpect(jsonPath("$.stats.totalCount").value(12))
                .andExpect(jsonPath("$.lastCompletedTimestamp").value("2026-06-08T19:13:00Z"));

        verify(service).getCampaignStatistics(SENDER_ID, CAMPAIGN_ID);
    }

    @Test
    void getCampaignStatisticsReturns404WhenStatisticsAreMissing() throws Exception {
        when(service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID))
                .thenThrow(new PnCampaignStatisticsNotFoundException("Statistics not found"));

        mockMvc.perform(get(PATH, CAMPAIGN_ID)
                        .header(SENDER_HEADER, SENDER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Campaign statistics not found"))
                .andExpect(jsonPath("$.errors[0].code")
                        .value(ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_STATISTICS_NOT_FOUND));

        verify(service).getCampaignStatistics(SENDER_ID, CAMPAIGN_ID);
    }

    @Test
    void getCampaignStatisticsReturns404WhenCampaignIsMissing() throws Exception {
        when(service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID))
                .thenThrow(new PnCampaignNotFoundException("Campaign not found"));

        mockMvc.perform(get(PATH, CAMPAIGN_ID)
                        .header(SENDER_HEADER, SENDER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Campaign not found"))
                .andExpect(jsonPath("$.errors[0].code")
                        .value(ERROR_CODE_WORKFLOWMANAGER_CAMPAIGN_NOT_FOUND));
    }

    @Test
    void getCampaignStatisticsReturns404WhenProviderCannotFindCampaign() throws Exception {
        when(service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID))
                .thenThrow(new it.pagopa.pn.commons.exceptions.PnCampaignNotFoundException("Campaign not found"));

        mockMvc.perform(get(PATH, CAMPAIGN_ID)
                        .header(SENDER_HEADER, SENDER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors[0].code")
                        .value(it.pagopa.pn.commons.exceptions.PnCampaignNotFoundException.ERROR_CODE_CAMPAIGN_NOT_FOUND));
    }

    @Test
    void getCampaignStatisticsKoRuntime() throws Exception {
        when(service.getCampaignStatistics(SENDER_ID, CAMPAIGN_ID))
                .thenThrow(new IllegalStateException("Unexpected failure"));

        mockMvc.perform(get(PATH, CAMPAIGN_ID)
                        .header(SENDER_HEADER, SENDER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getCampaignStatisticsRequiresSenderHeader() throws Exception {
        mockMvc.perform(get(PATH, CAMPAIGN_ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
