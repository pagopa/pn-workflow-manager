package it.pagopa.pn.workflowmanager.action.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.dto.timeline.details.AnalogDeliveryTypeInt;
import it.pagopa.pn.workflowmanager.dto.timeline.details.SendAnalogMessageDetailsInt;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.PhysicalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.NotificationSenderInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.CategorizedAttachmentsResultInt;
import it.pagopa.pn.workflowmanager.dto.ext.externalchannel.ResultFilterInt;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.AnalogDtoInt;
import it.pagopa.pn.workflowmanager.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.workflowmanager.dto.timeline.details.ServiceLevelInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import it.pagopa.pn.workflowmanager.service.TimelineService;
import it.pagopa.pn.workflowmanager.utils.SendAttachmentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaperChannelUtilsTest {

    private PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private TimelineUtils timelineUtils;
    private TimelineService timelineService;
    private PaperChannelUtils paperChannelUtils;
    private AttachmentUtils attachmentUtils;
    private CampaignService campaignService;
    private WorkflowUtils workflowUtils;

    private static final String IUN = "TEST-IUN-001";
    private static final Integer REC_INDEX = 0;
    private static final String EVENT_ID = "SEND_ANALOG.IUN_TEST-IUN-001.RECINDEX_0";
    private static final String PREPARE_REQUEST_ID = "PREPARE_REQUEST_001";

    @BeforeEach
    void setUp() {
        pnWorkflowManagerConfigs = mock(PnWorkflowManagerConfigs.class);
        timelineUtils = mock(TimelineUtils.class);
        timelineService = mock(TimelineService.class);
        attachmentUtils = mock(AttachmentUtils.class);
        campaignService = mock(CampaignService.class);
        workflowUtils = mock(WorkflowUtils.class);
        paperChannelUtils = new PaperChannelUtils(pnWorkflowManagerConfigs, timelineUtils, timelineService, attachmentUtils, campaignService, workflowUtils);
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
                replacedF24AttachmentUrls, categorizedAttachmentsResult, serviceLevelInt, AnalogDeliveryTypeInt.RS))
                .thenReturn(timelineElement);

        String result = paperChannelUtils.addSendAnalogNotificationToTimeline(
                notification, physicalAddress, REC_INDEX, analogDtoInfo,
                replacedF24AttachmentUrls, categorizedAttachmentsResult, serviceLevelInt, AnalogDeliveryTypeInt.RS);

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
    void getPrepareAnalogDeliveryTimelineElement_Success() {
        TimelineElementInternal expectedElement = TimelineElementInternal.builder()
                .elementId(EVENT_ID)
                .build();

        when(timelineService.getTimelineElement(IUN, EVENT_ID))
                .thenReturn(Optional.of(expectedElement));

        TimelineElementInternal result = paperChannelUtils.getPrepareAnalogDeliveryTimelineElement(IUN, EVENT_ID);

        assertEquals(expectedElement, result);
        verify(timelineService).getTimelineElement(IUN, EVENT_ID);
    }

    @Test
    void getPrepareAnalogDeliveryTimelineElement_NotFound_ThrowsException() {
        when(timelineService.getTimelineElement(IUN, EVENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(PnInternalException.class, () ->
                paperChannelUtils.getPrepareAnalogDeliveryTimelineElement(IUN, EVENT_ID)
        );

        verify(timelineService).getTimelineElement(IUN, EVENT_ID);
    }

    @Test
    void retrieveAttachmentsToSend_shouldReturnAttachments() {
        NotificationInt notification = mock(NotificationInt.class);
        int recIndex = 0;
        SendAttachmentMode sendAttachmentMode = mock(SendAttachmentMode.class);
        List<String> expectedAttachments = List.of("attachment1.pdf", "attachment2.pdf");

        when(attachmentUtils.retrieveAttachmentTypesToSend(notification, ChannelType.ANALOG))
                .thenReturn(sendAttachmentMode);
        when(attachmentUtils.retrieveAttachments(notification, recIndex, sendAttachmentMode, false))
                .thenReturn(expectedAttachments);

        List<String> result = paperChannelUtils.retrieveAttachmentsToSend(notification, recIndex);

        assertEquals(expectedAttachments, result);
        verify(attachmentUtils).retrieveAttachmentTypesToSend(notification, ChannelType.ANALOG);
        verify(attachmentUtils).retrieveAttachments(notification, recIndex, sendAttachmentMode, false);
        verifyNoMoreInteractions(attachmentUtils);
    }

    @Test
    void scheduleTimeoutForAnalogChannel_shouldScheduleTimeoutCorrectly() {
        String campaignId = "campaign-123";
        String paId = "pa-456";
        String iun = "IUN-789";
        int recIndex = 0;

        NotificationInt notification = mock(NotificationInt.class);
        NotificationSenderInt sender = mock(NotificationSenderInt.class);
        Campaign campaign = mock(Campaign.class);

        when(notification.getCampaignId()).thenReturn(campaignId);
        when(notification.getSender()).thenReturn(sender);
        when(sender.getPaId()).thenReturn(paId);
        when(notification.getIun()).thenReturn(iun);

        when(campaignService.getCampaignByCampaignIdAndSenderId(campaignId, paId))
                .thenReturn(campaign);

        paperChannelUtils.scheduleTimeoutForAnalogChannel(notification, recIndex);

        verify(campaignService).getCampaignByCampaignIdAndSenderId(campaignId, paId);
        verify(workflowUtils).scheduleTimeoutForCurrentChannel(iun, recIndex, campaign, ChannelType.ANALOG);
        verifyNoMoreInteractions(campaignService, workflowUtils);
    }

    @Test
    void getSendAnalogRequestIdFromPrepareRequestId_shouldReturnSendRequestId() {
        String sendRequestId = "SEND_REQUEST_001";

        TimelineElementInternal sendAnalogMessageTimelineElement = TimelineElementInternal.builder()
                .elementId(sendRequestId)
                .details(SendAnalogMessageDetailsInt.builder().prepareRequestId(PREPARE_REQUEST_ID).build())
                .build();

        when(timelineService.getTimeline(IUN, false))
                .thenReturn(Set.of(sendAnalogMessageTimelineElement));

        String result = paperChannelUtils.getSendAnalogRequestIdFromPrepareRequestId(IUN, PREPARE_REQUEST_ID);

        assertEquals(sendRequestId, result);
        verify(timelineService).getTimeline(IUN, false);
    }

    @Test
    void getSendAnalogRequestIdFromPrepareRequestId_shouldThrowExceptionWhenNotFound() {
        when(timelineService.getTimeline(IUN, false))
                .thenReturn(Set.of());

        assertThrows(PnInternalException.class, () ->
                paperChannelUtils.getSendAnalogRequestIdFromPrepareRequestId(IUN, PREPARE_REQUEST_ID)
        );

        verify(timelineService).getTimeline(IUN, false);
    }

}
