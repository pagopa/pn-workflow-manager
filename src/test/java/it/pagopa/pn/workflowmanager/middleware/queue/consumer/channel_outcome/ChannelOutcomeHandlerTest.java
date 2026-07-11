package it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome;

import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTrigger;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.channel_outcome.trigger.ChannelEventTriggerDispatcher;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.DesiredFeedbackType;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelOutcomeHandlerTest {

    @Mock
    private TimelineUtils timelineUtils;
    @Mock
    private TimelineService timelineService;
    @Mock
    private ChannelEventTriggerDispatcher channelEventTriggerDispatcher;
    @Mock
    private WorkflowUtils workflowUtils;

    @InjectMocks
    private ChannelOutcomeHandler channelOutcomeHandler;

    // Oggetti di test condivisi (Stub/Mocks dei parametri)
    @Mock
    private NormalizedChannelOutcome outcome;
    @Mock
    private NotificationInt notification;
    @Mock
    private Campaign campaign;
    @Mock
    private ChannelOutcomeClassification classification;
    @Mock
    private TimelineElementInternal timelineElement;
    @Mock
    private NotificationRecipientInt recipient;

    private final int recIndex = 0;
    private final String iun = "IUN-123-XYZ";
    private final ChannelType channel = ChannelType.PEC;
    private final String elementId = "ELEM-999";
    private final RecipientTypeInt recipientType = RecipientTypeInt.PF;
    private final Instant eventTimestamp = Instant.now();

    @BeforeEach
    void setUp() {
        // Configurazione comune per evitare ripetizioni nei singoli test
        lenient().when(outcome.getIun()).thenReturn(iun);
        lenient().when(outcome.getChannel()).thenReturn(channel);
        lenient().when(outcome.getRecIndex()).thenReturn(recIndex);
        lenient().when(outcome.getTimelineElementInternal()).thenReturn(timelineElement);
        lenient().when(outcome.getClassification()).thenReturn(classification);
        lenient().when(outcome.getEventTimestamp()).thenReturn(eventTimestamp);
        lenient().when(timelineElement.getElementId()).thenReturn(elementId);

        // Mock del destinatario dentro la notifica
        lenient().when(notification.getRecipients()).thenReturn(List.of(recipient));
        lenient().when(recipient.getRecipientType()).thenReturn(recipientType);
    }

    @Test
    void shouldAlwaysAddTimelineElementAndDispatchTriggersIfPresent() {
        // Arrange
        Set<ChannelEventTrigger> triggers = Set.of(mock(ChannelEventTrigger.class));
        when(outcome.getTriggers()).thenReturn(triggers);
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.empty());
        when(classification.getCategory()).thenReturn(ChannelOutcomeCategory.progress());

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(timelineService).addTimelineElement(timelineElement, notification);
        verify(channelEventTriggerDispatcher).dispatchAll(triggers, notification);
    }

    @Test
    void shouldPersistReachedElementWhenRecipientReached() {
        // Arrange
        when(outcome.getTriggers()).thenReturn(Collections.emptySet());
        when(classification.isRecipientReached()).thenReturn(true);
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.empty());
        when(classification.getCategory()).thenReturn(ChannelOutcomeCategory.progress());

        TimelineElementInternal reachedElement = mock(TimelineElementInternal.class);
        when(timelineUtils.buildDeliveredTimelineElement(notification, recIndex, channel, elementId, eventTimestamp))
                .thenReturn(reachedElement);

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(timelineService).addTimelineElement(reachedElement, notification);
        verify(timelineUtils).handleTransitionToReachedStatusIfNecessary(notification, recIndex, elementId);
    }

    @Test
    void shouldScheduleWorkflowDoneWhenDesiredFeedbackMatches() {
        // Arrange
        DesiredFeedbackType desiredFeedback = DesiredFeedbackType.RECEIVED;
        when(outcome.getTriggers()).thenReturn(Collections.emptySet());
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.of(desiredFeedback));

        when(workflowUtils.isDesiredFeedback(campaign, channel, desiredFeedback)).thenReturn(true);

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(workflowUtils).scheduleWorkflowDone(iun, recIndex, elementId, desiredFeedback);
        verify(workflowUtils, never()).advanceWorkflow(iun, recIndex, channel, campaign, recipientType);
    }

    @Test
    void shouldAdvanceWorkflowWhenFeedbackIsNegativeButNotDesired() {
        // Arrange
        when(outcome.getTriggers()).thenReturn(Collections.emptySet());
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.empty());
        when(classification.getCategory()).thenReturn(ChannelOutcomeCategory.negativeFeedback());

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(workflowUtils).advanceWorkflow(iun, recIndex, channel, campaign, recipientType);
        verify(workflowUtils, never()).scheduleWorkflowDone(eq(iun), eq(recIndex), eq(elementId), any());
    }

    @Test
    void shouldDoNothingElseWhenFeedbackIsPositiveButNotDesired() {
        // Arrange
        when(outcome.getTriggers()).thenReturn(Collections.emptySet());
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.empty());
        when(classification.getCategory()).thenReturn(ChannelOutcomeCategory.positiveFeedback());

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineService);
        verifyNoInteractions(workflowUtils);
    }

    @Test
    void shouldDoNothingElseWhenFeedbackIsNeitherFinalNorDesired() {
        // Arrange
        when(outcome.getTriggers()).thenReturn(Collections.emptySet());
        when(classification.getSatisfiedDesiredFeedback()).thenReturn(Optional.empty());
        when(classification.getCategory()).thenReturn(ChannelOutcomeCategory.progress());

        // Act
        channelOutcomeHandler.handleOutcome(outcome, notification, campaign);

        // Assert
        verify(timelineService, times(1)).addTimelineElement(timelineElement, notification);
        verifyNoMoreInteractions(timelineService);
        verifyNoInteractions(workflowUtils);
    }
}