package it.pagopa.pn.workflowmanager.rest;

import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.CampaignStatisticsResponse;
import it.pagopa.pn.workflowmanager.service.CampaignStatisticsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;


@WebFluxTest(CampaignStatisticsController.class)
@Import(CampaignStatisticsController.class)
class CampaignStatisticsControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private CampaignStatisticsService service;

    @MockitoBean
    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;

    @Test
    void getCampaignStatistics() {
        String streamId = UUID.randomUUID().toString();
        Mockito.when(service.getCampaignStatistics(Mockito.anyString()))
                .thenReturn(Mono.just(new CampaignStatisticsResponse()));

        webTestClient.get()
                .uri( ("/workflow-private/informal/{campaignId}/statistics").replace("{campaignId}", streamId) )
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CampaignStatisticsResponse.class);

        Mockito.verify(service).getCampaignStatistics(Mockito.anyString());

    }

    @Test
    void getCampaignStatisticsKoRuntime() {
        String streamId = UUID.randomUUID().toString();
        Mockito.when(service.getCampaignStatistics(Mockito.anyString()))
                .thenThrow(new NullPointerException());

        webTestClient.get()
                .uri( ("/workflow-private/informal/{campaignId}/statistics").replace("{campaignId}", streamId) )
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.Problem.class).consumeWith(
                        elem -> {
                            it.pagopa.pn.workflowmanager.generated.openapi.server.v1.dto.Problem problem = elem.getResponseBody();
                            assert problem != null;
                            Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
                        }
                );
    }
}
