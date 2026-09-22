package it.pagopa.pn.workflowmanager.service.impl;

import it.pagopa.pn.commons.db.campaign.CampaignServiceCachedProvider;
import it.pagopa.pn.commons.db.campaign.entity.CampaignEntity;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignInvalidStatus;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.Campaign;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.CampaignStatus;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.WorkFlowEntity;
import it.pagopa.pn.workflowmanager.service.CampaignService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class CampaignServiceImpl implements CampaignService {
    private final CampaignServiceCachedProvider campaignServiceCachedProvider;

    @Override
    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId) {
        CampaignEntity campaignEntity = campaignServiceCachedProvider.getByCampaignIdAndSenderId(campaignId, senderId);
        Campaign campaign = mapToCampaign(campaignEntity);
        if (!CampaignStatus.IN_PROGRESS.equals(campaign.getStatus())) {
            throw new PnCampaignInvalidStatus(campaignId, campaign.getStatus());
        }
        return campaign;
    }

    private Campaign mapToCampaign(CampaignEntity entity) {
        if (entity == null) {
            return null;
        }
        return Campaign.builder()
                .campaignId(entity.getCampaignId())
                .senderId(entity.getSenderId())
                .title(entity.getTitle())
                .descriptionScope(entity.getDescriptionScope())
                .startDate(entity.getStartDate() != null ?
                    OffsetDateTime.ofInstant(entity.getStartDate(), ZoneId.of("UTC")) : null)
                .endDate(entity.getEndDate() != null ?
                    OffsetDateTime.ofInstant(entity.getEndDate(), ZoneId.of("UTC")) : null)
                .status(entity.getStatus() != null ?
                    CampaignStatus.valueOf(entity.getStatus().name()) : null)
                .senderContact(entity.getSenderContact())
                .serviceId(entity.getServiceId())
                .serviceName(entity.getServiceName())
                .sensitiveContent(entity.getSensitiveContent())
                .stopOnViewed(entity.getStopOnViewed())
                .taxonomyCode(entity.getTaxonomyCode())
                .workflow(mapWorkflow(entity.getWorkflow()))
                .build();
    }

    private List<WorkFlowEntity> mapWorkflow(List<it.pagopa.pn.commons.db.campaign.entity.WorkflowEntity> workflow) {
        if (workflow == null) {
            return null;
        }
        return workflow.stream()
                .map(w -> WorkFlowEntity.builder()
                        .channel(w.getChannel() != null ?
                            it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType.valueOf(w.getChannel().name()) : null)
                        .recipientType(w.getRecipientType() != null ?
                            w.getRecipientType().stream()
                                    .map(rt -> it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt.valueOf(rt.name()))
                                    .collect(Collectors.toSet()) : null)
                        .timeout(w.getTimeout())
                        .includeAttachment(w.getIncludeAttachment())
                        .desiredFeedback(w.getDesiredFeedback() != null ?
                            w.getDesiredFeedback().stream()
                                    .map(df -> it.pagopa.pn.workflowmanager.dto.ext.campaign.DesiredFeedbackType.valueOf(df.name()))
                                    .collect(Collectors.toSet()) : null)
                        .build())
                .collect(Collectors.toList());
    }
}
