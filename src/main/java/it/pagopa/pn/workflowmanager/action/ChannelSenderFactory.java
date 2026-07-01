package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ChannelSenderFactory {
    public ChannelSender getChannelSender(ChannelType channel){
        return null;
    }
}
