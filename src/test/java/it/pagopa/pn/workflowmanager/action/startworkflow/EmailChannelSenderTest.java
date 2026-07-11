package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChannelSenderTest {
    private static final String IUN = "IUN_TEST_123";
    private static final String EMAIL_ADDRESS = "test@example.com";
    private static final String HTML_CONTENT = "<html>test</html>";
    private static final String EMAIL_SUBJECT = "Test Subject";

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PnExternalChannelsClient pnExternalChannelsClient;

    @Mock
    private TemplateGeneratorService templateGeneratorService;

    @Mock
    private ChannelSenderUtils channelSenderUtils;

    @Mock
    private WorkflowUtils workflowUtils;

    @InjectMocks
    private EmailChannelSender emailChannelSender;

    @Test
    void shouldReturnCorrectChannelType() {
        assertEquals(ChannelType.EMAIL, emailChannelSender.getChannelType());
    }

    @Test
    void shouldSendEmailWithAttachmentsWhenEmailPresentAndIncludeAttachmentTrue() {
        // Given
        NotificationInt notification = buildNotification(EMAIL_ADDRESS);
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = buildCampaign(true);
        int recIndex = 0;
        int currentStep = 0;

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.EMAIL);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(templateGeneratorService.generateEmailBodyTemplate(notification, recipient, campaign))
                .thenReturn(HTML_CONTENT);
        when(templateGeneratorService.generateEmailSubjectTemplate(notification, recipient))
                .thenReturn(EMAIL_SUBJECT);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_EMAIL),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        when(channelSenderUtils.resolveAttachmentsForChannel(notification, recIndex, currentStep, campaign, ChannelType.EMAIL))
                .thenReturn(List.of("safestorage://doc1", "safestorage://doc2"));

        // When
        emailChannelSender.send(notification, campaign, recIndex, currentStep);

        // Then
        verify(pnExternalChannelsClient).sendNotificationEMAIL(
                eq(expectedRequestId),
                eq(HTML_CONTENT),
                eq(EMAIL_SUBJECT),
                eq(notification),
                any(NotificationRecipientInt.class),
                argThat(addr -> EMAIL_ADDRESS.equals(addr.getAddress())),
                eq(List.of("safestorage://doc1", "safestorage://doc2"))
        );
        verify(channelSenderUtils).saveSendDigitalMessageElement(
                eq(notification), eq(expectedRequestId), eq(recIndex),
                any(InformalDigitalAddressInt.class),
                eq(DigitalChannelsInt.EMAIL),
                eq(DigitalAddressSourceInt.SPECIAL)
        );
        verify(workflowUtils).scheduleTimeoutForCurrentChannel(IUN, recIndex, campaign, ChannelType.EMAIL);
        verify(auditLogEvent).generateSuccess("Email sent successfully");
    }

    @Test
    void shouldSendEmailWithoutAttachmentsWhenIncludeAttachmentFalse() {
        // Given
        NotificationInt notification = buildNotification(EMAIL_ADDRESS);
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = buildCampaign(false);
        int recIndex = 0;
        int currentStep = 0;

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.EMAIL);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);


        when(templateGeneratorService.generateEmailBodyTemplate(notification, recipient, campaign))
                .thenReturn(HTML_CONTENT);
        when(templateGeneratorService.generateEmailSubjectTemplate(notification, recipient))
                .thenReturn(EMAIL_SUBJECT);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_EMAIL),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // When
        emailChannelSender.send(notification, campaign, recIndex, currentStep);

        // Then
        verify(pnExternalChannelsClient).sendNotificationEMAIL(
                eq(expectedRequestId),
                eq(HTML_CONTENT),
                eq(EMAIL_SUBJECT),
                eq(notification),
                any(NotificationRecipientInt.class),
                any(InformalDigitalAddressInt.class),
                eq(List.of())
        );
        verify(channelSenderUtils).resolveAttachmentsForChannel(any(), anyInt(), anyInt(), any(), any());
        verify(workflowUtils).scheduleTimeoutForCurrentChannel(IUN, recIndex, campaign, ChannelType.EMAIL);
    }

    @Test
    void shouldSkipAndAdvanceWorkflowWhenEmailMissing() {
        // Given
        NotificationInt notification = buildNotification(null);
        Campaign campaign = buildSimpleCampaign();
        int recIndex = 0;
        int currentStep = 0;

        String expectedSkipRequestId = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(
                recIndex, IUN, ChannelType.EMAIL);

        // When
        emailChannelSender.send(notification, campaign, recIndex, currentStep);

        // Then
        verify(channelSenderUtils).saveSendDigitalMessageSkipElement(
                eq(recIndex),
                eq(notification),
                eq(expectedSkipRequestId),
                eq(DigitalChannelsInt.EMAIL),
                eq(DigitalAddressSourceInt.SPECIAL)
        );
        verify(workflowUtils).advanceWorkflow(
                eq(IUN),
                eq(recIndex),
                eq(ChannelType.EMAIL),
                eq(campaign),
                eq(RecipientTypeInt.PF)
        );
        verifyNoInteractions(pnExternalChannelsClient, templateGeneratorService, auditLogService);
    }

    // Helper methods
    private NotificationInt buildNotification(String email) {
        NotificationInt notification = mock(NotificationInt.class);
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .email(email)
                .recipientType(RecipientTypeInt.PF)
                .build();

        when(notification.getIun()).thenReturn(IUN);
        when(notification.getRecipients()).thenReturn(List.of(recipient));
        return notification;
    }

    private Campaign buildCampaign(boolean includeAttachment) {
        WorkFlowEntity workflowEntity = WorkFlowEntity.builder()
                .includeAttachment(includeAttachment)
                .build();

        return Campaign.builder()
                .workflow(List.of(workflowEntity)).build();
    }

    private Campaign buildSimpleCampaign() {
        return mock(Campaign.class);
    }
}
