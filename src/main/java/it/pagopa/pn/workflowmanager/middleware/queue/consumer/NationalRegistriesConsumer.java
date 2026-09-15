package it.pagopa.pn.workflowmanager.middleware.queue.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressSQSMessage;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.nationalregistries.model.AddressSQSMessageDigitalAddressInner;
import it.pagopa.pn.workflowmanager.middleware.responsehandler.NationalRegistriesResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.List;

import static it.pagopa.pn.workflowmanager.middleware.queue.consumer.utils.MdcUtils.setMdc;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class NationalRegistriesConsumer {
    private final PnWorkflowManagerConfigs pnWorkflowManagerConfigs;
    private final NationalRegistriesResponseHandler nationalRegistriesResponseHandler;

    @SqsListener(queueNames = "#{@pnWorkflowManagerConfigs.topics.nationalRegistriesEvents}")
    public void pnNationalRegistriesEventInboundConsumer(Message<AddressSQSMessage> message) {
        setMdc(message);
        try {
            log.info("Handle message from {} with content {}", NationalRegistriesClient.CLIENT_NAME, message);

            List<AddressSQSMessageDigitalAddressInner> digitalAddresses = message.getPayload().getDigitalAddress();
            String correlationId = message.getPayload().getCorrelationId();
            NationalRegistriesResponse response = NationalRegistriesMessageUtil.buildPublicRegistryResponse(correlationId, digitalAddresses);
            nationalRegistriesResponseHandler.handleResponse(response);

        } catch (Exception ex) {
            HandleEventUtils.handleException(message.getHeaders(), ex);
            throw ex;
        }
    }
}
