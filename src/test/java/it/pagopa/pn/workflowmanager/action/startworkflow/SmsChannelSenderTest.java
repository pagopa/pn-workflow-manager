package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.action.utils.WorkflowUtils;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.DigitalChannelsInt;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.externalchannel.PnExternalChannelsClient;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.TemplateGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsChannelSenderTest {

    private static final String IUN = "IUN_TEST_123";
    private static final String PHONE_NUMBER = "+39333123456";
    private static final String SMS_CONTENT = "SMS test content";

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ChannelSenderUtils channelSenderUtils;

    @Mock
    private WorkflowUtils workflowUtils;

    @Mock
    private TemplateGeneratorService templateGeneratorService;

    @Mock
    private PnExternalChannelsClient pnExternalChannelsClient;

    @InjectMocks
    private SmsChannelSender smsChannelSender;

    @Test
    void shouldReturnCorrectChannelType() {
        assertEquals(ChannelType.SMS, smsChannelSender.getChannelType());
    }

    @Test
    void shouldSendSmsWhenPhoneNumberPresent() {
        // Given
        NotificationInt notification = buildNotification(PHONE_NUMBER);
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = mock(Campaign.class);
        int recIndex = 0;
        int currentStep = 0;

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.SMS,0);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(templateGeneratorService.generateSmsTemplate(notification, recipient)).thenReturn(SMS_CONTENT);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_SMS),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // When
        smsChannelSender.send(notification, campaign, recIndex, currentStep);

        // Then
        verify(pnExternalChannelsClient).sendNotificationSMS(
                eq(expectedRequestId),
                eq(SMS_CONTENT),
                eq(PHONE_NUMBER)
        );
        verify(channelSenderUtils).saveSendDigitalMessageElement(
                eq(notification), eq(expectedRequestId), eq(recIndex),
                any(InformalDigitalAddressInt.class),
                eq(DigitalChannelsInt.SMS),
                eq(DigitalAddressSourceInt.SPECIAL)
        );
        verify(workflowUtils).scheduleTimeoutForCurrentChannel(IUN, recIndex, campaign, ChannelType.SMS);
        verify(auditLogEvent).generateSuccess("Sms sent successfully");
    }

    @Test
    void shouldSkipAndAdvanceWorkflowWhenPhoneNumberMissing() {
        // Given
        NotificationInt notification = buildNotification(null);
        Campaign campaign = mock(Campaign.class);
        int recIndex = 0;
        int currentStep = 0;

        String expectedSkipRequestId = ChannelSenderUtils.buildSendDigitalMessageSkipTimelineElementId(
                recIndex, IUN, ChannelType.SMS);

        // When
        smsChannelSender.send(notification, campaign, recIndex, currentStep);

        // Then
        verify(channelSenderUtils).saveSendDigitalMessageSkipElement(
                eq(recIndex),
                eq(notification),
                eq(expectedSkipRequestId),
                eq(DigitalChannelsInt.SMS)
        );
        verify(workflowUtils).advanceWorkflow(
                eq(IUN),
                eq(recIndex),
                eq(ChannelType.SMS),
                eq(campaign),
                eq(RecipientTypeInt.PF)
        );
        verifyNoInteractions(pnExternalChannelsClient, templateGeneratorService, auditLogService);
    }

    @Test
    void shouldThrowPnInternalExceptionAndLogFailureWhenSmsSendingFails() {
        // Given
        NotificationInt notification = buildNotification(PHONE_NUMBER);
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = mock(Campaign.class);
        int recIndex = 0;
        int currentStep = 0;

        String expectedRequestId = ChannelSenderUtils.buildSendDigitalMessageEventId(IUN, recIndex, ChannelType.SMS,0);
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(templateGeneratorService.generateSmsTemplate(notification, recipient)).thenReturn(SMS_CONTENT);
        when(auditLogService.buildAuditLogEvent(eq(IUN), eq(recIndex), eq(PnAuditLogEventType.AUD_COM_SEND_SMS),
                anyString(), eq(IUN), eq(recIndex), eq(expectedRequestId)))
                .thenReturn(auditLogEvent);
        doThrow(new RuntimeException("external service error"))
                .when(pnExternalChannelsClient).sendNotificationSMS(anyString(), anyString(), anyString());
        when(auditLogEvent.generateFailure(anyString(), any())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // When / Then
        assertThrows(PnInternalException.class,
                () -> smsChannelSender.send(notification, campaign, recIndex, currentStep));

        verify(auditLogEvent).generateFailure(eq("Error sending SMS notification"), any(RuntimeException.class));
        verify(channelSenderUtils, never()).saveSendDigitalMessageElement(any(), any(), anyInt(), any(), any(), any());
        verify(workflowUtils, never()).scheduleTimeoutForCurrentChannel(any(), anyInt(), any(), any());
    }

    // Helper methods
    private NotificationInt buildNotification(String phoneNumber) {
        NotificationInt notification = mock(NotificationInt.class);
        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .phoneNumber(phoneNumber)
                .recipientType(RecipientTypeInt.PF)
                .build();

        when(notification.getIun()).thenReturn(IUN);
        when(notification.getRecipients()).thenReturn(List.of(recipient));
        return notification;
    }
}
