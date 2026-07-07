package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;

public interface IoConnectorClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_IO_CONNECTOR;

    String SEND_MESSAGE = "SEND MESSAGE";

    void sendMessage(IoMessageRequest ioMessageRequest);
}
