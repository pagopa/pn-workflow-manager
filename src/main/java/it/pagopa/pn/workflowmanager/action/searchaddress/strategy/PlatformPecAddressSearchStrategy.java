package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.mapper.AddressMapper;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.service.AddressBookService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@AllArgsConstructor
@CustomLog
public class PlatformPecAddressSearchStrategy implements SyncAddressSearchStrategy {
    private final AddressBookService addressBookService;

    @Override
    public Set<SourceChannelKey> supportedKeys() {
        return new HashSet<>(Set.of(
                new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.PEC)
        ));
    }

    @Override
    public SourceSearchOutcome search(AddressSearchContext context) {
        String recipientId = context.notification().getRecipients().get(context.recipientIndex()).getInternalId();
        String senderId = context.notification().getSender().getPaId();
        String cxId = context.notification().getRecipients().get(context.recipientIndex()).getRecipientType().getValue();

        log.info("Starting PLATFORM PEC address search - iun={} recipientIndex={} senderId={} recipientId={}",
                context.notification().getIun(), context.recipientIndex(), senderId, recipientId);

        if (addressBookService.areMandatoryConsentsAccepted(recipientId, cxId)) {
            log.info("TOS not accepted for PLATFORM PEC search - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.tosNotAccepted(DigitalAddressSourceInt.PLATFORM);
        }

        return getPlatformAddresses(recipientId, senderId);
    }

    public SourceSearchOutcome getPlatformAddresses(String recipientId, String senderId) {
        Optional<LegalDigitalAddressInt> optLegalDigitalAddress = addressBookService.getPlatformAddresses(recipientId, senderId);

        log.info("GetLegalAddress OK - senderId={}", senderId);

        if(optLegalDigitalAddress.isEmpty()) {
            log.debug("PLATFORM PEC address not found - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);
        }

        LegalDigitalAddressInt legalDigitalAddress = optLegalDigitalAddress.get();
        InformalDigitalAddressInt informalDigitalAddress = AddressMapper.externalToInternal(legalDigitalAddress);
        log.info("PLATFORM PEC address found - senderId={} recipientId={} type={}", senderId, recipientId, informalDigitalAddress.getType());
        return SourceSearchOutcome.found(DigitalAddressSourceInt.PLATFORM, informalDigitalAddress);

    }
}
