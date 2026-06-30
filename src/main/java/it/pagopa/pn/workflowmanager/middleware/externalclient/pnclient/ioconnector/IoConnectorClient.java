package it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.ioconnector;

import it.pagopa.pn.workflowmanager.dto.client.IoMessageRequest;

public interface IoConnectorClient {
    // TODO: String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_IO_CONNECTOR;
    String CLIENT_NAME = "pn-io-connector";

    String SEND_MESSAGE = "SEND MESSAGE";

    void sendMessage(IoMessageRequest ioMessageRequest);
}
