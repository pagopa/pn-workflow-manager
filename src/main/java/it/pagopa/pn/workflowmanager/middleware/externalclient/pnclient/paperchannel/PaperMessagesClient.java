package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.deliverypushworkflow.generated.openapi.msclient.paperchannel.model.SendResponse;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelPrepareRequest;
import it.pagopa.pn.workflowmanager.dto.ext.paperchannel.PaperChannelSendRequest;

public interface PaperMessagesClient {
    String CLIENT_ID = "pn-workflow-manager";
    String CLIENT_NAME = "pn-paper-messages";
    String PREPARE_ANALOG_NOTIFICATION = "PREPARE ANALOG NOTIFICATION";
    String SEND_ANALOG_NOTIFICATION = "SEND ANALOG NOTIFICATION";
    String PRINT_TYPE_BN_FRONTE_RETRO = "BN_FRONTE_RETRO";

    void prepare(PaperChannelPrepareRequest paperChannelPrepareRequest);
    SendResponse send(PaperChannelSendRequest paperChannelSendRequest);
}
