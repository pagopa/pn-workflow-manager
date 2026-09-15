package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.mapper.AddressMapper;
import it.pagopa.pn.workflowmanager.action.sendcourtesy.InformalCourtesyAddressResolver;
import it.pagopa.pn.workflowmanager.dto.address.CourtesyDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
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
public class PlatformEmailSmsAddressSearchStrategy implements SyncAddressSearchStrategy {

    private final InformalCourtesyAddressResolver informalCourtesyAddressResolver;
    private final AddressBookService addressBookService;

    @Override
    public Set<SourceChannelKey> supportedKeys() {
        return new HashSet<>(Set.of(
                new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.SMS),
                new SourceChannelKey(DigitalAddressSourceInt.PLATFORM, ChannelType.EMAIL)
        ));
    }

    @Override
    public SourceSearchOutcome search(AddressSearchContext context) {
        String recipientId = context.notification().getRecipients().get(context.recipientIndex()).getInternalId();
        String senderId = context.notification().getSender().getPaId();
        String cxId = context.notification().getRecipients().get(context.recipientIndex()).getRecipientType().getValue();
        String channel = context.channel().name();

        log.info("Starting PLATFORM EMAIL/SMS address search - iun={} recipientIndex={} senderId={} recipientId={} channel={}",
                context.notification().getIun(), context.recipientIndex(), senderId, recipientId, context.channel());

        if (addressBookService.areMandatoryConsentsAccepted(recipientId, cxId)) {
            log.info("TOS not accepted for PLATFORM EMAIL/SMS search - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.tosNotAccepted(DigitalAddressSourceInt.PLATFORM);
        }

        return getCourtesyAddressBySender(recipientId, senderId, channel);
    }

    private SourceSearchOutcome getCourtesyAddressBySender(String recipientId, String senderId, String channel) {
        Optional<CourtesyDigitalAddressInt> optCourtesyAddress = informalCourtesyAddressResolver.resolveAddressByType(recipientId, senderId, CourtesyDigitalAddressInt.COURTESY_DIGITAL_ADDRESS_TYPE_INT.valueOf(channel));
        if (optCourtesyAddress.isEmpty()) {
            log.debug("Courtesy digital addresses not found - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);
        }

        CourtesyDigitalAddressInt courtesyAddress = optCourtesyAddress.get();

        InformalDigitalAddressInt address = AddressMapper.externalToInternal(courtesyAddress);

        log.debug("For senderId={} address type={} is available", senderId, address.getType());
        log.info("PLATFORM EMAIL/SMS address found - senderId={} recipientId={} type={}", senderId, recipientId, address.getType());

        return SourceSearchOutcome.found(DigitalAddressSourceInt.PLATFORM, address);
    }
}
