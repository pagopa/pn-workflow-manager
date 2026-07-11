package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendRelatedTimelineElement;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.io.IoEventNormalizer;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.IoOutcomeEvent;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelEventProcessorTest {
    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CampaignService campaignService;
    @Mock
    private IoEventNormalizer ioEventNormalizer;
    @Mock
    private ChannelOutcomeHandler channelOutcomeHandler;

    @InjectMocks
    private ChannelEventProcessor channelEventProcessor;

    // Mock delle entità e dei DTO coinvolti nel flusso
    @Mock
    private IoOutcomeEvent event;
    @Mock
    private NotificationInt notificationInt;
    @Mock
    private NotificationSenderInt sender;
    @Mock
    private Campaign campaign;
    @Mock
    private NormalizedChannelOutcome normalizedChannelOutcome;

    private final String requestId = "req-io-111";
    private final String iun = "IUN-IO-222";
    private final SendRelatedTimelineElement sourceSendRequestDetails = mock(SendRelatedTimelineElement.class);
    private final String campaignId = "camp-01";
    private final String paId = "pa-id-abc";
    private final PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

    @BeforeEach
    void setUp() {
        // Configurazione degli stub per l'evento e l'estrazione delle chiavi
        when(event.getRequestId()).thenReturn(requestId);
        when(timelineUtils.getIunFromTimelineId(requestId)).thenReturn(iun);
        when(timelineUtils.checkAndRetrieveSourceSendRequestDetails(iun, requestId)).thenReturn(sourceSendRequestDetails);

        // Configurazione degli stub per il recupero dati (Notification e Campaign)
        when(notificationService.getInformalNotificationByIun(iun)).thenReturn(notificationInt);
        when(notificationInt.getCampaignId()).thenReturn(campaignId);
        when(notificationInt.getSender()).thenReturn(sender);
        when(sender.getPaId()).thenReturn(paId);
        when(campaignService.getCampaignByCampaignIdAndSenderId(campaignId, paId)).thenReturn(campaign);

        // Configurazione della normalizzazione
        when(normalizedChannelOutcome.getPnAuditLogEvent()).thenReturn(auditLogEvent);
        when(ioEventNormalizer.normalize(event, notificationInt, sourceSendRequestDetails)).thenReturn(normalizedChannelOutcome);
    }

    @Test
    void shouldOrchestrateChannelEventHandlingSuccessfully() {
        // Act
        channelEventProcessor.process(event, ioEventNormalizer);

        // Assert - Verifica l'estrazione e il recupero sequenziale dei dati dai servizi
        verify(timelineUtils).getIunFromTimelineId(requestId);
        verify(timelineUtils).checkAndRetrieveSourceSendRequestDetails(iun, requestId);
        verify(notificationService).getInformalNotificationByIun(iun);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignId, paId);

        // Assert - Verifica che l'evento sia stato passato al normalizzatore corretto
        verify(ioEventNormalizer).normalize(event, notificationInt, sourceSendRequestDetails);

        // Assert - Verifica che l'esito normalizzato sia stato consegnato al ChannelOutcomeHandler terminale
        verify(channelOutcomeHandler).handleOutcome(normalizedChannelOutcome, notificationInt, campaign);
        verify(auditLogEvent).generateSuccess("Channel outcome event processed successfully");
    }

    @Test
    void shouldOrchestrateChannelEventHandlingWithError() {
        Exception simulatedException = new RuntimeException("Simulated processing error");
        doThrow(simulatedException)
                .when(channelOutcomeHandler).handleOutcome(normalizedChannelOutcome, notificationInt, campaign);
        // Act
        Assertions.assertThrows(RuntimeException.class, () -> channelEventProcessor.process(event, ioEventNormalizer));

        // Assert - Verifica l'estrazione e il recupero sequenziale dei dati dai servizi
        verify(timelineUtils).getIunFromTimelineId(requestId);
        verify(timelineUtils).checkAndRetrieveSourceSendRequestDetails(iun, requestId);
        verify(notificationService).getInformalNotificationByIun(iun);
        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignId, paId);

        // Assert - Verifica che l'evento sia stato passato al normalizzatore corretto
        verify(ioEventNormalizer).normalize(event, notificationInt, sourceSendRequestDetails);

        // Assert - Verifica che sia generato un log di errore e che l'eccezione sia stata propagata
        verify(auditLogEvent).generateFailure("Error processing channel outcome event: Simulated processing error", simulatedException);
    }
}