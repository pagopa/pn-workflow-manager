package it.pagopa.pn.workflowmanager.action.startworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.utils.ChannelSenderUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.SaveDocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalogChannelSenderTest {

    private static final String IUN = "TEST-IUN-001";
    private static final int REC_INDEX = 0;
    private static final int CURRENT_STEP = 0;
    private static final String FILE_KEY = "coverpage-file-key-123";

    @Mock
    private SaveDocumentService saveDocumentService;

    @Mock
    private ChannelSenderUtils channelSenderUtils;

    @InjectMocks
    private AnalogChannelSender analogChannelSender;

    @Test
    void shouldReturnCorrectChannelType() {
        assertEquals(ChannelType.ANALOG, analogChannelSender.getChannelType());
    }

    @Test
    void send_Success() {
        // Given
        NotificationInt notification = buildNotification();
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = mock(Campaign.class);

        when(saveDocumentService.saveCoverpage(
                eq(notification), eq(recipient), eq(campaign), anyString(), eq(REC_INDEX)))
                .thenReturn(FILE_KEY);

        // When
        analogChannelSender.send(notification, campaign, REC_INDEX, CURRENT_STEP);

        // Then
        verify(saveDocumentService).saveCoverpage(
                eq(notification), eq(recipient), eq(campaign), anyString(), eq(REC_INDEX));
        verify(channelSenderUtils).saveCoverpageCreationElement(
                notification, REC_INDEX, FILE_KEY);
    }

    @Test
    void send_Failure_ThrowsPnInternalException() {
        // Given
        NotificationInt notification = buildNotification();
        NotificationRecipientInt recipient = notification.getRecipients().getFirst();
        Campaign campaign = mock(Campaign.class);

        when(saveDocumentService.saveCoverpage(
                eq(notification), eq(recipient), eq(campaign), anyString(), eq(REC_INDEX)))
                .thenThrow(new RuntimeException("Save coverpage error"));

        // When & Then
        assertThrows(PnInternalException.class, () ->
                analogChannelSender.send(notification, campaign, REC_INDEX, CURRENT_STEP)
        );

        verify(saveDocumentService).saveCoverpage(
                eq(notification), eq(recipient), eq(campaign), anyString(), eq(REC_INDEX));
        verify(channelSenderUtils, never()).saveCoverpageCreationElement(any(), anyInt(), anyString());
    }

    private NotificationInt buildNotification() {
        PhysicalAddressInt physicalAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .province("RM")
                .build();

        NotificationRecipientInt recipient = NotificationRecipientInt.builder()
                .physicalAddress(physicalAddress)
                .build();

        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn(IUN);
        when(notification.getRecipients()).thenReturn(List.of(recipient));

        return notification;
    }
}
