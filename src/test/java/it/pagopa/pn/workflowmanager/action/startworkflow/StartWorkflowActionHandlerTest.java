package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchOrchestrator;
import it.pagopa.pn.workflowmanager.dto.action.details.StartWorkflowDetails;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartWorkflowActionHandlerTest {

    @Mock
    private ChannelSenderFactory channelSenderFactory;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ChannelSender channelSender;

    @Mock
    private AddressSearchOrchestrator addressSearchOrchestrator;

    private StartWorkflowActionHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final int TEST_REC_INDEX = 0;
    private static final String TEST_PA_ID = "PA-001";
    private static final ChannelType TEST_CHANNEL_DIGITAL = ChannelType.IO;
    private static final Instant TEST_SENT_AT = Instant.parse("2026-09-15T07:37:10.836Z");

    @BeforeEach
    void setup() {
        handler = new StartWorkflowActionHandler(
                channelSenderFactory,
                notificationService,
                addressSearchOrchestrator
        );
    }

    @Test
    void startWorkflowAction_shouldTriggerAddressSearch() {
        // Arrange
        StartWorkflowDetails details = createStartWorkflowDetails();
        NotificationInt notification = createMockNotification();

        when(channelSenderFactory.getChannelSender(TEST_CHANNEL_DIGITAL)).thenReturn(channelSender);
        when(channelSender.getChannelType()).thenReturn(TEST_CHANNEL_DIGITAL);
        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);

        // Act
        handler.startWorkflowAction(TEST_IUN, TEST_REC_INDEX, details);

        // Assert
        verify(addressSearchOrchestrator).handle(any(AddressSearchContext.class));
    }


    private StartWorkflowDetails createStartWorkflowDetails() {
        StartWorkflowDetails details = new StartWorkflowDetails();
        details.setChannel(TEST_CHANNEL_DIGITAL);
        return details;
    }

    private NotificationInt createMockNotification() {
        NotificationSenderInt sender = NotificationSenderInt.builder()
                .paId(TEST_PA_ID)
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .recipientType(RecipientTypeInt.PF)
                .build();

        return NotificationInt.builder()
                .iun(TEST_IUN)
                .sender(sender)
                .recipients(List.of(recipient))
                .sentAt(TEST_SENT_AT)
                .build();
    }
}
