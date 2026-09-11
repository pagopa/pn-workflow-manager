package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@CustomLog
public class SpecialAddressSearchStrategy implements SyncAddressSearchStrategy {

    @Override
    public Set<SourceChannelKey> supportedKeys() {
        return new HashSet<>(Set.of(
                new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.PEC),
                new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.SMS),
                new SourceChannelKey(DigitalAddressSourceInt.SPECIAL, ChannelType.EMAIL)
        ));
    }

    @Override
    public SourceSearchOutcome search(AddressSearchContext context) {
        log.info("Starting SPECIAL address search - iun={} recipientIndex={} channel={}",
                context.notification().getIun(), context.recipientIndex(), context.channel());
        String address;
        InformalDigitalAddressInt informalAddress;
        switch (context.channel()) {
            case ChannelType.PEC:
                address = context.notification().getRecipients().get(context.recipientIndex()).getDigitalDomicile().getAddress();
                informalAddress = buildInformalAddress(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC, address);
                log.info("SPECIAL address found - channel={} type={}", context.channel(), informalAddress.getType());
                return SourceSearchOutcome.found(DigitalAddressSourceInt.SPECIAL, informalAddress);

            case ChannelType.SMS:
                address = context.notification().getRecipients().get(context.recipientIndex()).getPhoneNumber();

                informalAddress = buildInformalAddress(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.SMS, address);
                log.info("SPECIAL address found - channel={} type={}", context.channel(), informalAddress.getType());
                return SourceSearchOutcome.found(DigitalAddressSourceInt.SPECIAL, informalAddress);

            case ChannelType.EMAIL:
                address = context.notification().getRecipients().get(context.recipientIndex()).getEmail();

                informalAddress = buildInformalAddress(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.EMAIL, address);
                log.info("SPECIAL address found - channel={} type={}", context.channel(), informalAddress.getType());
                return SourceSearchOutcome.found(DigitalAddressSourceInt.SPECIAL, informalAddress);

            default:
                log.debug("SPECIAL address not found - unsupported channel={}", context.channel());
                return SourceSearchOutcome.notFound(DigitalAddressSourceInt.SPECIAL);
        }
    }

    private InformalDigitalAddressInt buildInformalAddress(InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE addressType, String address) {
        return InformalDigitalAddressInt.builder()
                .type(addressType)
                .address(address)
                .build();
    }
}
