package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaperChannelUtilsTest {

    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private TimelineUtils timelineUtils;
    private TimelineService timelineService;
    private PaperChannelUtils paperChannelUtils;

    private static final String IUN = "TEST-IUN-001";
    private static final Integer REC_INDEX = 0;
    private static final String EVENT_ID = "SEND_ANALOG.IUN_TEST-IUN-001.RECINDEX_0";
    private static final String PREPARE_REQUEST_ID = "PREPARE_REQUEST_001";

    @BeforeEach
    void setUp() {
        pnWorkflowManagerConfigs = mock(PnWorkflowManagerConfigs.class);
        timelineUtils = mock(TimelineUtils.class);
        timelineService = mock(TimelineService.class);
        paperChannelUtils = new PaperChannelUtils(pnWorkflowManagerConfigs, timelineUtils, timelineService);
    }

    @Test
    void getSenderAddress_Success() {
        PhysicalAddressInt expectedAddress = PhysicalAddressInt.builder()
                .address("Via Roma 1")
                .zip("00100")
                .municipality("Roma")
                .build();

        PnWorkflowManagerConfigs.PaperChannel paperChannel = mock(PnWorkflowManagerConfigs.PaperChannel.class);
        when(pnWorkflowManagerConfigs.getPaperChannel()).thenReturn(paperChannel);
        when(paperChannel.getSenderPhysicalAddress()).thenReturn(expectedAddress);

        PhysicalAddressInt result = paperChannelUtils.getSenderAddress();

        assertEquals(expectedAddress, result);
        verify(pnWorkflowManagerConfigs).getPaperChannel();
    }

    @Test
    void addSendAnalogNotificationToTimeline_Success() {
        NotificationInt notification = mock(NotificationInt.class);
        PhysicalAddressInt physicalAddress = mock(PhysicalAddressInt.class);
        AnalogDtoInt analogDtoInfo = mock(AnalogDtoInt.class);
        List<String> replacedF24AttachmentUrls = List.of();
        CategorizedAttachmentsResultInt categorizedAttachmentsResult = mock(CategorizedAttachmentsResultInt.class);
        ServiceLevelInt serviceLevelInt = mock(ServiceLevelInt.class);

        TimelineElementInternal timelineElement = TimelineElementInternal.builder()
                .elementId(EVENT_ID)
                .build();

        when(timelineUtils.buildSendAnalogNotificationTimelineElement(
                physicalAddress, REC_INDEX, notification, analogDtoInfo,
                replacedF24AttachmentUrls, categorizedAttachmentsResult, serviceLevelInt, PREPARE_REQUEST_ID))
                .thenReturn(timelineElement);

        String result = paperChannelUtils.addSendAnalogNotificationToTimeline(
                notification, physicalAddress, REC_INDEX, analogDtoInfo,
                replacedF24AttachmentUrls, categorizedAttachmentsResult, PREPARE_REQUEST_ID, serviceLevelInt);

        assertEquals(EVENT_ID, result);
        verify(timelineService).addTimelineElement(timelineElement, notification);
    }

    @Test
    void getAttachments_Success() {
        ResultFilterInt attachment1 = mock(ResultFilterInt.class);
        ResultFilterInt attachment2 = mock(ResultFilterInt.class);
        when(attachment1.getFileKey()).thenReturn("file1.pdf");
        when(attachment2.getFileKey()).thenReturn("file2.pdf");

        CategorizedAttachmentsResultInt categorizedAttachmentsResult = mock(CategorizedAttachmentsResultInt.class);
        when(categorizedAttachmentsResult.getAcceptedAttachments())
                .thenReturn(List.of(attachment1, attachment2));

        List<String> result = PaperChannelUtils.getAttachments(categorizedAttachmentsResult);

        assertEquals(2, result.size());
        assertEquals("file1.pdf", result.get(0));
        assertEquals("file2.pdf", result.get(1));
    }

    @Test
    void buildAnalogDto_Success() {
        String productType = "AR_REGISTERED_LETTER";
        SendResponse sendResponse = mock(SendResponse.class);

        AnalogDtoInt result = PaperChannelUtils.buildAnalogDto(PREPARE_REQUEST_ID, productType, sendResponse);

        assertNotNull(result);
        assertEquals(0, result.getSentAttemptMade());
        assertEquals(sendResponse, result.getSendResponse());
        assertEquals(productType, result.getProductType());
        assertEquals(PREPARE_REQUEST_ID, result.getPrepareRequestId());
        assertNull(result.getRelatedRequestId());
    }

    @Test
    void getPaperChannelNotificationTimelineElement_Success() {
        TimelineElementInternal expectedElement = TimelineElementInternal.builder()
                .elementId(EVENT_ID)
                .build();

        when(timelineService.getTimelineElement(IUN, EVENT_ID))
                .thenReturn(Optional.of(expectedElement));

        TimelineElementInternal result = paperChannelUtils.getPaperChannelNotificationTimelineElement(IUN, EVENT_ID);

        assertEquals(expectedElement, result);
        verify(timelineService).getTimelineElement(IUN, EVENT_ID);
    }

    @Test
    void getPaperChannelNotificationTimelineElement_NotFound_ThrowsException() {
        when(timelineService.getTimelineElement(IUN, EVENT_ID))
                .thenReturn(Optional.empty());

        verify(timelineService).getTimelineElement(IUN, EVENT_ID);
    }
}
