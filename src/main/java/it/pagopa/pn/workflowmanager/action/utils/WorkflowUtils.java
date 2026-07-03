package it.pagopa.pn.workflowmanager.action.utils;


import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.RecipientTypeInt;
import it.pagopa.pn.workflowmanager.models.internal.campaign.Campaign;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.models.internal.campaign.WorkFlowEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class WorkflowUtils {

    public Optional<NextChannel> getNextChannel(Campaign campaign, ChannelType channelType, RecipientTypeInt recipientTypeInt) {
        List<WorkFlowEntity> filteredSteps = campaign.getWorkflow().stream()
                .filter(step -> step.getRecipientType().contains(recipientTypeInt))
                .toList();

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


    public record NextChannel(ChannelType channel, int stepIndex) {
    }
}
