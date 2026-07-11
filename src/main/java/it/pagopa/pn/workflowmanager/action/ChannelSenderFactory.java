package it.pagopa.pn.workflowmanager.action;

import it.pagopa.pn.workflowmanager.action.start_workflow.AnalogChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.EmailChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.IoChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.PecChannelSender;
import it.pagopa.pn.workflowmanager.action.start_workflow.SmsChannelSender;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelSenderFactory {
    private final IoChannelSender ioChannelSender;
    private final EmailChannelSender emailChannelSender;
    private final PecChannelSender pecChannelSender;
    private final SmsChannelSender smsChannelSender;
    private final AnalogChannelSender analogChannelSender;

    public ChannelSender getChannelSender(@Nonnull ChannelType channel){
        return switch (channel) {
            case IO -> ioChannelSender;
            case PEC -> pecChannelSender;
            case EMAIL -> emailChannelSender;
            case SMS -> smsChannelSender;
            case ANALOG -> analogChannelSender;
        };
    }
}
