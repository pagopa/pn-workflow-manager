package it.pagopa.pn.workflowmanager.action.searchaddress.strategy;

import it.pagopa.pn.workflowmanager.action.searchaddress.AddressSearchContext;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceChannelKey;
import it.pagopa.pn.workflowmanager.action.searchaddress.dto.SourceSearchOutcome;
import it.pagopa.pn.workflowmanager.action.searchaddress.mapper.AddressMapper;
import it.pagopa.pn.workflowmanager.config.PnWorkflowManagerConfigs;
import it.pagopa.pn.workflowmanager.dto.address.DigitalAddressSourceInt;
import it.pagopa.pn.workflowmanager.dto.address.InformalDigitalAddressInt;
import it.pagopa.pn.workflowmanager.dto.ext.campaign.ChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyChannelType;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.addressbook.model.CourtesyDigitalAddress;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.Consent;
import it.pagopa.pn.workflowmanager.generated.openapi.msclient.userattributes.consents.model.CxTypeAuthFleet;
import it.pagopa.pn.workflowmanager.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
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
    private final PnWorkflowManagerConfigs configs;

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

        log.info("Starting PLATFORM EMAIL/SMS address search - iun={} recipientIndex={} senderId={} recipientId={} channel={}",
                context.notification().getIun(), context.recipientIndex(), senderId, recipientId, context.channel());

        if (consentsKo(recipientId, cxId)) {
            log.info("TOS not accepted for PLATFORM EMAIL/SMS search - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.tosNotAccepted(DigitalAddressSourceInt.PLATFORM);
        }

        return getCourtesyAddressBySender(recipientId, senderId);
    }

    private SourceSearchOutcome getCourtesyAddressBySender(String recipientId, String senderId) {
        List<CourtesyDigitalAddress> courtesyAddresses = userAttributesClient.getCourtesyAddressBySender(recipientId, senderId);

        courtesyAddresses = courtesyAddresses.stream().filter(address -> !CourtesyChannelType.APPIO.equals(address.getChannelType())).toList();

        if (CollectionUtils.isEmpty(courtesyAddresses)) {
            log.debug("Courtesy digital addresses not found - senderId={} recipientId={}", senderId, recipientId);
            return SourceSearchOutcome.notFound(DigitalAddressSourceInt.PLATFORM);
        }

        if (courtesyAddresses.size() > 1) {
            log.warn("Digital addresses list contains more than one element - senderId={}", senderId);
        }

        InformalDigitalAddressInt address = courtesyAddresses.stream().map(
                AddressMapper::externalToInternal
        ).findFirst().orElseThrow();

        log.debug("For senderId={} address type={} is available", senderId, address.getType());
        log.info("PLATFORM EMAIL/SMS address found - senderId={} recipientId={} type={}", senderId, recipientId, address.getType());

        return SourceSearchOutcome.found(DigitalAddressSourceInt.PLATFORM, address);
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
}
