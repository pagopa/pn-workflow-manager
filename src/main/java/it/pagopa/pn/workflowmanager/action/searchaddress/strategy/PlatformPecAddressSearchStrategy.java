package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.mapper.AddressMapper;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.LegalDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.CxTypeAuthFleet;
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
public class PlatformPecAddressSearchStrategy implements SyncAddressSearchStrategy {

    private final PnUserAttributesClient userAttributesClient;
    private final PnWorkflowManagerConfigs configs;

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

        if (consentsKo(recipientId, cxId)) {
            log.info("TOS not accepted for PLATFORM PEC search - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.tosNotAccepted(DigitalAddressSourceInt.PLATFORM);
        }

        return getPlatformAddresses(recipientId, senderId);
    }

    private boolean consentsKo(String recipientId, String cxId) {
        List<Consent> contents = userAttributesClient.getConsents(recipientId, CxTypeAuthFleet.fromValue(cxId));

        boolean emptyConsents = CollectionUtils.isEmpty(contents);
        return emptyConsents ||
                        contents.stream().noneMatch(consent -> configs.getConsentsForPlatformSearch().stream().anyMatch(configConsent ->
                                configConsent.getType().equals(consent.getConsentType().getValue()) &&
                                        configConsent.getVersion().equals(consent.getConsentVersion())
                        ));
    }

    public SourceSearchOutcome getPlatformAddresses(String recipientId, String senderId) {
        List<LegalDigitalAddress> legalDigitalAddresses = userAttributesClient.getLegalAddressBySender(recipientId, senderId);

        log.info("GetLegalAddress OK - senderId={}", senderId);


        if (legalDigitalAddresses != null && !legalDigitalAddresses.isEmpty()) {

            if (legalDigitalAddresses.size() > 1) {
                log.warn("Digital addresses list contains more than one element - senderId={}", senderId);
            }

            List<InformalDigitalAddressInt> digitalAddresses = legalDigitalAddresses.stream().map(
                    AddressMapper::externalToInternal
            ).toList();

            for (InformalDigitalAddressInt address : digitalAddresses) {
                log.debug("For senderId={} address type={} is available", senderId, address.getType());

                if (InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.PEC.equals(address.getType()) || InformalDigitalAddressInt.INFORMAL_DIGITAL_ADDRESS_TYPE.SERCQ.equals(address.getType())) {
                    log.info("PLATFORM PEC address found - senderId={} recipientId={} type={}", senderId, recipientId, address.getType());
                    return SourceSearchOutcome.found(DigitalAddressSourceInt.PLATFORM, address);
                }
            }
        }

        log.debug("list legal address is empty - senderId={}", senderId);
        return SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);

    }
}
