package it.pagopa.pn.workflowmanager.dto.ext.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

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
}


