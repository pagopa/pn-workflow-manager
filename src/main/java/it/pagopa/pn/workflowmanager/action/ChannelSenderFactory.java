package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.start_workflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.PecChannelSender;
import it.pagopa.pn.workflowmanager.models.internal.campaign.ChannelType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelSenderFactory {
    private final IoChannelSender ioChannelSender;
    private final PecChannelSender pecChannelSender;

    public ChannelSender getChannelSender(@Nonnull ChannelType channel){
        return switch (channel) {
            case IO -> ioChannelSender;
            case PEC -> pecChannelSender;
            default -> throw new IllegalArgumentException("Unsupported channel type: " + channel);
        };
    }
}
