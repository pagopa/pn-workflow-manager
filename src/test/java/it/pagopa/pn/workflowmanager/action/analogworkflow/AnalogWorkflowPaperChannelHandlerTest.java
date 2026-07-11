package it.pagopa.pn.workflowmanager.action.analogworkflow;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.workflowmanager.action.utils.PaperChannelUtils;
import it.pagopa.pn.workflowmanager.action.utils.TimelineUtils;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.PrepareAnalogDeliveryDetailsInt;
import it.pagopa.pn.workflowmanager.exceptions.PnPaperChannelChangedCostException;
import it.pagopa.pn.workflowmanager.middleware.queue.consumer.event.PrepareEventInt;
import it.pagopa.pn.workflowmanager.service.AuditLogService;
import it.pagopa.pn.workflowmanager.service.NotificationService;
import it.pagopa.pn.workflowmanager.service.PaperChannelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalogWorkflowPaperChannelHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaperChannelService paperChannelService;

    @Mock
    private PaperChannelUtils paperChannelUtils;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private TimelineUtils timelineUtils;

    private AnalogWorkflowPaperChannelResponseHandler handler;

    private static final String TEST_IUN = "TEST-IUN-001";
    private static final String TEST_REQUEST_ID = "PREPARE_ANALOG_DELIVERY.IUN_TEST-IUN-001.RECINDEX_0.ATTEMPT_0.DELIVERYTYPE_RS";
    private static final Integer REC_INDEX = 0;
    private static final String PRODUCT_TYPE = "AR";
    private static final String TIMELINE_ID = "SEND_ANALOG.IUN_TEST-IUN-001.RECINDEX_0";

    @BeforeEach
    void setup() {
        handler = new AnalogWorkflowPaperChannelResponseHandler(
                notificationService,
                paperChannelService,
                paperChannelUtils,
                auditLogService,
                timelineUtils
        );
    }

    @Test
    void paperChannelPrepareResponseHandler_OK_Success() {
        // GIVEN
        PrepareEventInt prepareEvent = createPrepareEvent(PrepareEventInt.STATUS_CODE.OK.name());
        NotificationInt notification = mock(NotificationInt.class);
        TimelineElementInternal timelineElement = createTimelineElement();
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(paperChannelUtils.getPrepareAnalogDeliveryTimelineElement(TEST_IUN, TEST_REQUEST_ID))
                .thenReturn(timelineElement);
        when(auditLogService.buildAuditLogEvent(eq(TEST_IUN), eq(REC_INDEX),
                eq(PnAuditLogEventType.AUD_COM_PD_PREPARE_RECEIVE), anyString(), eq(TEST_REQUEST_ID), anyString()))
                .thenReturn(auditLogEvent);
        when(paperChannelService.sendSimpleRegisteredLetter(
                eq(notification), eq(REC_INDEX), eq(TEST_REQUEST_ID), any(PhysicalAddressInt.class),
                eq(PRODUCT_TYPE), anyList(), any(CategorizedAttachmentsResultInt.class)))
                .thenReturn(TIMELINE_ID);
        when(auditLogEvent.generateSuccess(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.paperChannelPrepareResponseHandler(prepareEvent)
        );

        // THEN
        verify(paperChannelService).sendSimpleRegisteredLetter(
                eq(notification), eq(REC_INDEX), eq(TEST_REQUEST_ID), any(PhysicalAddressInt.class),
                eq(PRODUCT_TYPE), anyList(), any(CategorizedAttachmentsResultInt.class));
        verify(auditLogEvent).generateSuccess(anyString());
        verify(auditLogEvent).log();
        verify(paperChannelUtils).scheduleTimeoutForAnalogChannel(notification, REC_INDEX);
    }

    @Test
    void paperChannelPrepareResponseHandler_KO_ThrowsException() {
        // GIVEN
        PrepareEventInt prepareEvent = createPrepareEvent(PrepareEventInt.STATUS_CODE.KO.name());
        NotificationInt notification = mock(NotificationInt.class);
        TimelineElementInternal timelineElement = createTimelineElement();
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(paperChannelUtils.getPrepareAnalogDeliveryTimelineElement(TEST_IUN, TEST_REQUEST_ID))
                .thenReturn(timelineElement);
        when(auditLogService.buildAuditLogEvent(eq(TEST_IUN), eq(REC_INDEX),
                eq(PnAuditLogEventType.AUD_COM_PD_PREPARE_RECEIVE), anyString(), eq(TEST_REQUEST_ID), anyString()))
                .thenReturn(auditLogEvent);
        when(auditLogEvent.generateFailure(anyString(), any())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // WHEN & THEN
        Assertions.assertThrows(PnInternalException.class, () ->
                handler.paperChannelPrepareResponseHandler(prepareEvent)
        );

        verify(paperChannelService, never()).sendSimpleRegisteredLetter(
                any(), anyInt(), anyString(), any(), anyString(), anyList(), any());
        verify(auditLogEvent).generateFailure(anyString(), any());
        verify(auditLogEvent).log();
    }

    @Test
    void paperChannelPrepareResponseHandler_OK_CostChanged_CatchException() {
        // GIVEN
        PrepareEventInt prepareEvent = createPrepareEvent(PrepareEventInt.STATUS_CODE.OK.name());
        NotificationInt notification = mock(NotificationInt.class);
        when(notification.getIun()).thenReturn(TEST_IUN);

        TimelineElementInternal timelineElement = createTimelineElement();
        PnAuditLogEvent auditLogEvent = mock(PnAuditLogEvent.class);
        String coverpageFileKey = "coverpage-key-123";
        Throwable exception = new PnPaperChannelChangedCostException();

        when(notificationService.getInformalNotificationByIun(TEST_IUN)).thenReturn(notification);
        when(paperChannelUtils.getPrepareAnalogDeliveryTimelineElement(TEST_IUN, TEST_REQUEST_ID))
                .thenReturn(timelineElement);
        when(auditLogService.buildAuditLogEvent(eq(TEST_IUN), eq(REC_INDEX),
                eq(PnAuditLogEventType.AUD_COM_PD_PREPARE_RECEIVE), anyString(), eq(TEST_REQUEST_ID), anyString()))
                .thenReturn(auditLogEvent);
        when(paperChannelService.sendSimpleRegisteredLetter(
                eq(notification), eq(REC_INDEX), eq(TEST_REQUEST_ID), any(PhysicalAddressInt.class),
                eq(PRODUCT_TYPE), anyList(), any(CategorizedAttachmentsResultInt.class)))
                .thenThrow(exception);
        when(timelineUtils.retrieveCoverpageFileKey(TEST_IUN, REC_INDEX)).thenReturn(coverpageFileKey);
        when(auditLogEvent.generateWarning(anyString())).thenReturn(auditLogEvent);
        when(auditLogEvent.log()).thenReturn(auditLogEvent);

        // WHEN
        Assertions.assertDoesNotThrow(() ->
                handler.paperChannelPrepareResponseHandler(prepareEvent)
        );

        // THEN
        verify(paperChannelService).sendSimpleRegisteredLetter(
                eq(notification), eq(REC_INDEX), eq(TEST_REQUEST_ID), any(PhysicalAddressInt.class),
                eq(PRODUCT_TYPE), anyList(), any(CategorizedAttachmentsResultInt.class));
        verify(paperChannelService).prepareSimpleRegisteredLetter(notification, REC_INDEX, coverpageFileKey);
        verify(auditLogEvent).generateWarning(anyString());
        verify(auditLogEvent).log();
    }

    private PrepareEventInt createPrepareEvent(String statusCode) {
        PhysicalAddressInt receiverAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .build();

        return PrepareEventInt.builder()
                .iun(TEST_IUN)
                .requestId(TEST_REQUEST_ID)
                .statusCode(statusCode)
                .statusDetail("Status detail")
                .statusDateTime(Instant.now())
                .receiverAddress(receiverAddress)
                .productType(PRODUCT_TYPE)
                .replacedF24AttachmentUrls(List.of())
                .categorizedAttachmentsResult(mock(CategorizedAttachmentsResultInt.class))
                .build();
    }

    private TimelineElementInternal createTimelineElement() {
        PrepareAnalogDeliveryDetailsInt details = PrepareAnalogDeliveryDetailsInt.builder()
                .recIndex(AnalogWorkflowPaperChannelHandlerTest.REC_INDEX)
                .deliveryType(AnalogDeliveryTypeInt.RS)
                .build();

        return TimelineElementInternal.builder()
                .elementId(TEST_REQUEST_ID)
                .details(details)
                .build();
    }
}