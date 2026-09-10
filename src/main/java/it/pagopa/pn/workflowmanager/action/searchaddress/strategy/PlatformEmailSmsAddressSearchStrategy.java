package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.mapper.AddressMapper;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@CustomLog
public class PlatformEmailSmsAddressSearchStrategy implements SyncAddressSearchStrategy {

    private final PnUserAttributesClient userAttributesClient;

    @Override
    public Set<SourceChannelKey> supportedKeys() {
        return new HashSet<>(Set.of(
                new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.SMS),
                new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.EMAIL)
        ));
    }

    @Override
    public SourceSearchOutcome search(AddressSearchContext context) {
        //TODO aggiungere logs

        String recipientId = context.notification().getRecipients().get(context.recipientIndex()).getInternalId();
        String senderId = context.notification().getSender().getPaId();
        List<CourtesyDigitalAddress> courtesyAddresses = userAttributesClient.getCourtesyAddressBySender(recipientId, senderId);

        courtesyAddresses = courtesyAddresses.stream().filter(address -> !CourtesyChannelType.APPIO.equals(address.getChannelType())).toList();

        if (CollectionUtils.isEmpty(courtesyAddresses)) {
            return SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);
        }

        if (courtesyAddresses.size() > 1) {
            log.warn("Digital addresses list contains more than one element - senderId={}", senderId);
        }

        InformalDigitalAddressInt address = courtesyAddresses.stream().map(
                AddressMapper::externalToInternal
        ).findFirst().orElseThrow();

        log.debug("For senderId={} address type={} is available", senderId, address.getType());

        return SourceSearchOutcome.found(DigitalAddressSourceInt.PLATFORM, address);

    }
}
