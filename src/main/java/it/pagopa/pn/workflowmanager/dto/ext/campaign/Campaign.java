package it.pagopa.pn.workflowmanager.dto.ext.campaign;

import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.exceptions.PnWorkflowException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    private String campaignId;
    private String senderId;
    private String title;
    private String descriptionScope;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private CampaignStatus status;
    private String senderContact;
    private String serviceId;
    private String serviceName;
    private Boolean sensitiveContent;
    private Boolean stopOnViewed;
    private String taxonomyCode;
    private List<WorkFlowEntity> workflow;

    public WorkFlowEntity getWorkflowByChannel(ChannelType channelType) {
        if(workflow == null) {
            throw new PnWorkflowException(String.format("No workflow configured for campaignId %s", campaignId));
        }

        return workflow.stream()
                .filter(workFlowEntity -> workFlowEntity.getChannel().equals(channelType))
                .findFirst()
                .orElseThrow(() -> new PnWorkflowException(String.format("No workflow step found for channel %s in campaignId %s", channelType, campaignId)));
    }

    public List<WorkFlowEntity> getWorkflowsByRecipientType(RecipientTypeInt recipientType) {
        if(workflow == null) {
            throw new PnWorkflowException(String.format("No workflow configured for campaignId %s", campaignId));
        }

        List<WorkFlowEntity> filteredWorkflows = workflow.stream()
                .filter(workFlowEntity -> workFlowEntity.getRecipientType() != null && workFlowEntity.getRecipientType().contains(recipientType))
                .toList();

        if(filteredWorkflows.isEmpty()) {
            throw new PnWorkflowException(String.format("No workflow step found for recipientType %s in campaignId %s", recipientType, campaignId));
        }

        return filteredWorkflows;
    }

    public Optional<NextChannel> getNextChannel(ChannelType channelType, RecipientTypeInt recipientType) {
        List<WorkFlowEntity> filteredSteps = getWorkflowsByRecipientType(recipientType);

        for (int i = 0; i < filteredSteps.size(); i++) {
            if (filteredSteps.get(i).getChannel().equals(channelType)) {
                if (i < filteredSteps.size() - 1) {
                    ChannelType nextChannel = filteredSteps.get(i + 1).getChannel();
                    return Optional.of(new NextChannel(nextChannel, i + 1));
                }
                break;
            }
        }
        return Optional.empty();
    }
}


