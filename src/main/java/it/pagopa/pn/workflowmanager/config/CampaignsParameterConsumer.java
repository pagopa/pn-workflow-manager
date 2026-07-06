package it.pagopa.pn.workflowmanager.config;

import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.exceptions.PnCampaignNotFoundException;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import java.util.*;

@Slf4j
@Configuration
public class CampaignsParameterConsumer {

    private static final String PARAMETER_STORE_MVP_CAMPAIGNS = "MVPCampaigns";

    private final ParameterConsumer parameterConsumer;
    private List<Campaign> campaigns = Collections.emptyList();

    public CampaignsParameterConsumer(ParameterConsumer parameterConsumer) {
        this.parameterConsumer = parameterConsumer;
    }

    @PostConstruct
    protected void initialize() {
        Optional<Campaign[]> maybeCampaigns = loadCampaigns();

        if (maybeCampaigns.isEmpty()) {
            log.info("No campaign configuration found on parameter store");
            return;
        }

        List<Campaign> loaded = new ArrayList<>();
        for (Campaign campaign : maybeCampaigns.get()) {
            if (isValid(campaign)) {
                log.info("Adding campaign configuration to in-memory load list campaignId={}, senderId={}",
                        campaign.getCampaignId(), campaign.getSenderId());
                loaded.add(campaign);
            } else {
                log.warn("Invalid campaign configuration found: {}", campaign);
            }
        }
        campaigns = Collections.unmodifiableList(loaded);

        log.info("Loaded {} campaigns in memory", campaigns.size());
    }

    private Optional<Campaign[]> loadCampaigns() {
        try {
            return parameterConsumer.getParameterValue(
                    PARAMETER_STORE_MVP_CAMPAIGNS,
                    Campaign[].class
            );
        } catch (PnInternalException ex) {
            if (hasParameterNotFoundCause(ex)) {
                log.info("Campaign configuration parameter {} not found on parameter store", PARAMETER_STORE_MVP_CAMPAIGNS);
                return Optional.empty();
            }
            throw ex;
        }
    }

    public Campaign getCampaignByCampaignIdAndSenderId(String campaignId, String senderId) {
        return campaigns.stream()
                .filter(campaign -> Objects.equals(campaignId, campaign.getCampaignId())
                        && Objects.equals(senderId, campaign.getSenderId()))
                .findFirst()
                .orElseThrow(() -> new PnCampaignNotFoundException(
                        String.format("Campaign with campaignId=%s and senderId=%s not found", campaignId, senderId)
                ));
    }

    private boolean isValid(Campaign campaign) {
        return Objects.nonNull(campaign)
                && StringUtils.hasText(campaign.getCampaignId())
                && isValidSenderId(campaign.getSenderId())
                && StringUtils.hasText(campaign.getTitle())
                && StringUtils.hasText(campaign.getDescriptionScope())
                && Objects.nonNull(campaign.getStartDate())
                && Objects.nonNull(campaign.getEndDate())
                && Objects.nonNull(campaign.getStatus())
                && StringUtils.hasText(campaign.getServiceId())
                && hasValidWorkflow(campaign.getWorkflow());
    }

    private boolean isValidSenderId(String senderId) {
        if (!StringUtils.hasText(senderId)) {
            return false;
        }

        try {
            UUID.fromString(senderId);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean hasValidWorkflow(List<WorkFlowEntity> workflow) {
        return Objects.nonNull(workflow)
                && workflow.stream().allMatch(this::hasValidWorkflowStep);
    }

    private boolean hasValidWorkflowStep(WorkFlowEntity workflowStep) {
        return Objects.nonNull(workflowStep)
                && Objects.nonNull(workflowStep.getChannel())
                && Objects.nonNull(workflowStep.getRecipientType())
                && !workflowStep.getRecipientType().isEmpty()
                && workflowStep.getRecipientType().stream().allMatch(Objects::nonNull)
                && Objects.nonNull(workflowStep.getDesiredFeedback())
                && Objects.nonNull(workflowStep.getIncludeAttachment());
    }

    private boolean hasParameterNotFoundCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ParameterNotFoundException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}