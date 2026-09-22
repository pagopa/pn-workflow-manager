package it.pagopa.pn.workflowmanager.action.sendcourtesy.registry;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.sender.CourtesyAddressSender;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.delivery.notification.CommunicationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.pn.workflowmanager.exceptions.WorkflowManagerExceptionCodes.ERROR_CODE_WORKFLOWMANAGER_COURTESYERROR;

@Component
@Slf4j
public class CourtesyChannelSenderRegistry {
    private final Map<CourtesyChannelKey, CourtesyAddressSender> senders;

    CourtesyChannelSenderRegistry(List<CourtesyAddressSender> all) {
        this.senders = all.stream().collect(Collectors.toMap(
                s -> new CourtesyChannelKey(s.getCommunicationType(), s.getCourtesyAddressType()),
                Function.identity(),
                (s1, s2) -> {
                    throw new PnInternalException("Duplicated sender for domain=" + s1.getCommunicationType() + " channel=" + s1.getCourtesyAddressType(), ERROR_CODE_WORKFLOWMANAGER_COURTESYERROR);
                })
        );
    }

    public CourtesyAddressSender getSender(CommunicationType domain, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT channel) {
        CourtesyAddressSender sender = senders.get(new CourtesyChannelKey(domain, channel));
        if (sender == null) {
            throw new PnInternalException("Nessun sender registrato per domain=" + domain + " channel=" + channel, ERROR_CODE_WORKFLOWMANAGER_COURTESYERROR);
        }
        return sender;
    }
}
